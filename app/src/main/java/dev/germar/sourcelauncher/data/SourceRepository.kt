package dev.germar.sourcelauncher.data

import android.content.Context
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.os.Handler
import android.os.Looper
import dev.germar.sourcelauncher.model.ConnectionState
import dev.germar.sourcelauncher.model.SourceKind
import dev.germar.sourcelauncher.model.TvSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Provides the TV's inputs, or a fixed demo set when demo mode is on (emulators have no HDMI). */
class SourceRepository(
    context: Context,
    settingsRepository: SettingsRepository,
) {
    private val appContext = context.applicationContext
    private val tvInputManager: TvInputManager? = appContext.getSystemService(TvInputManager::class.java)
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val sources: Flow<List<TvSource>> = settingsRepository.settings
        .map { it.demoMode to it.showTunerInputs }
        .distinctUntilChanged()
        .flatMapLatest { (demoMode, includeTuners) ->
            if (demoMode) flowOf(DemoSources.all) else tvInputs(includeTuners)
        }

    val isTvInputFrameworkAvailable: Boolean get() = tvInputManager != null

    /** Re-reads the inputs, e.g. when the home screen becomes visible and CEC names may have changed. */
    fun refresh() {
        refreshRequests.tryEmit(Unit)
    }

    private fun tvInputs(includeTuners: Boolean): Flow<List<TvSource>> = callbackFlow {
        val manager = tvInputManager
        if (manager == null) {
            send(emptyList())
            awaitClose()
            return@callbackFlow
        }
        val callback = object : TvInputManager.TvInputCallback() {
            override fun onInputStateChanged(inputId: String, state: Int) { trySend(query(manager, includeTuners)) }
            override fun onInputAdded(inputId: String) { trySend(query(manager, includeTuners)) }
            override fun onInputRemoved(inputId: String) { trySend(query(manager, includeTuners)) }
            override fun onInputUpdated(inputId: String) { trySend(query(manager, includeTuners)) }
            override fun onTvInputInfoUpdated(inputInfo: TvInputInfo) { trySend(query(manager, includeTuners)) }
        }
        send(query(manager, includeTuners))
        manager.registerCallback(callback, Handler(Looper.getMainLooper()))
        val refreshJob = launch { refreshRequests.collect { send(query(manager, includeTuners)) } }
        awaitClose {
            refreshJob.cancel()
            manager.unregisterCallback(callback)
        }
    }.conflate().distinctUntilChanged()

    private fun query(manager: TvInputManager, includeTuners: Boolean): List<TvSource> {
        val inputs = runCatching { manager.tvInputList }.getOrDefault(emptyList())
        val ids = inputs.map { it.id }.toSet()
        // HDMI-CEC devices show up as child inputs of the HDMI port they are plugged into.
        val childrenByParent = inputs
            .filter { it.parentId != null && it.parentId in ids }
            .groupBy { it.parentId!! }
        return inputs
            .filter { info ->
                val isRoot = info.parentId == null || info.parentId !in ids
                val isWanted = info.isPassthroughInput || includeTuners
                isRoot && isWanted
            }
            .map { root -> toSource(manager, root, childrenByParent[root.id].orEmpty()) }
            .sortedWith(compareBy<TvSource>({ it.kind.sortOrder }, { it.portLabel.filterNot(Char::isDigit) }, { it.portLabel.filter(Char::isDigit).toIntOrNull() ?: 0 }))
    }

    private fun toSource(manager: TvInputManager, root: TvInputInfo, children: List<TvInputInfo>): TvSource {
        val deviceLabels = children.flatMap { child ->
            listOfNotNull(
                child.loadCustomLabel(appContext)?.toString(),
                child.loadLabel(appContext)?.toString(),
            )
        }.filter { it.isNotBlank() }.distinct()
        return TvSource(
            id = root.id,
            kind = kindOf(root.type),
            portLabel = root.loadLabel(appContext)?.toString().orEmpty().ifBlank { root.id.substringAfterLast('/') },
            customLabel = root.loadCustomLabel(appContext)?.toString()?.takeIf { it.isNotBlank() },
            deviceLabels = deviceLabels,
            connection = connectionOf(manager, root, children),
            isPassthrough = root.isPassthroughInput,
            hiddenBySystem = runCatching { root.isHidden(appContext) }.getOrDefault(false),
        )
    }

    private fun connectionOf(manager: TvInputManager, root: TvInputInfo, children: List<TvInputInfo>): ConnectionState {
        val rootState = stateOf(manager, root.id)
        val childStates = children.map { stateOf(manager, it.id) }
        return when {
            ConnectionState.CONNECTED in childStates -> ConnectionState.CONNECTED
            ConnectionState.STANDBY in childStates -> ConnectionState.STANDBY
            else -> rootState
        }
    }

    private fun stateOf(manager: TvInputManager, id: String): ConnectionState =
        when (runCatching { manager.getInputState(id) }.getOrNull()) {
            TvInputManager.INPUT_STATE_CONNECTED -> ConnectionState.CONNECTED
            TvInputManager.INPUT_STATE_CONNECTED_STANDBY -> ConnectionState.STANDBY
            TvInputManager.INPUT_STATE_DISCONNECTED -> ConnectionState.DISCONNECTED
            else -> ConnectionState.UNKNOWN
        }

    private fun kindOf(type: Int): SourceKind = when (type) {
        TvInputInfo.TYPE_HDMI -> SourceKind.HDMI
        TvInputInfo.TYPE_COMPONENT -> SourceKind.COMPONENT
        TvInputInfo.TYPE_COMPOSITE -> SourceKind.COMPOSITE
        TvInputInfo.TYPE_SVIDEO -> SourceKind.SVIDEO
        TvInputInfo.TYPE_SCART -> SourceKind.SCART
        TvInputInfo.TYPE_VGA -> SourceKind.VGA
        TvInputInfo.TYPE_DVI -> SourceKind.DVI
        TvInputInfo.TYPE_DISPLAY_PORT -> SourceKind.DISPLAY_PORT
        TvInputInfo.TYPE_TUNER -> SourceKind.TUNER
        else -> SourceKind.OTHER
    }

    private val SourceKind.sortOrder: Int
        get() = when (this) {
            SourceKind.HDMI -> 0
            SourceKind.TUNER -> 2
            else -> 1
        }
}

object DemoSources {
    const val PREFIX = "demo:"

    val all: List<TvSource> = listOf(
        TvSource("${PREFIX}hdmi1", SourceKind.HDMI, "HDMI 1", deviceLabels = listOf("Apple TV"), connection = ConnectionState.CONNECTED),
        TvSource("${PREFIX}hdmi2", SourceKind.HDMI, "HDMI 2", deviceLabels = listOf("PlayStation 5"), connection = ConnectionState.STANDBY),
        TvSource("${PREFIX}hdmi3", SourceKind.HDMI, "HDMI 3", deviceLabels = listOf("Steam Deck"), connection = ConnectionState.CONNECTED),
        TvSource("${PREFIX}hdmi4", SourceKind.HDMI, "HDMI 4", connection = ConnectionState.DISCONNECTED),
    )

    fun isDemo(id: String): Boolean = id.startsWith(PREFIX)
}
