package dev.germar.sourcelauncher.ui

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.germar.sourcelauncher.appGraph
import dev.germar.sourcelauncher.model.AutoSelectPolicy
import dev.germar.sourcelauncher.model.LauncherSettings
import dev.germar.sourcelauncher.model.ResolvedSource
import dev.germar.sourcelauncher.model.SourceOrder
import dev.germar.sourcelauncher.model.SourceOverride
import dev.germar.sourcelauncher.model.SourceResolver
import dev.germar.sourcelauncher.service.HomeButtonService
import dev.germar.sourcelauncher.service.HomeGuard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Screens of the single-activity UI. The back stack lives in the ViewModel. */
sealed interface Screen {
    val key: String

    data object Home : Screen { override val key = "home" }
    data object Settings : Screen { override val key = "settings" }
    data class SourceEdit(val id: String) : Screen { override val key = "edit:$id" }
    data class Rename(val id: String) : Screen { override val key = "rename:$id" }
    data class IconPicker(val id: String?) : Screen { override val key = "icons:$id" }
    data class ColorPicker(val id: String) : Screen { override val key = "colors:$id" }
    data object Reorder : Screen { override val key = "reorder" }
    data object DefaultPicker : Screen { override val key = "default" }
    data object DelayPicker : Screen { override val key = "delay" }
    data object ClockPicker : Screen { override val key = "clock" }
    data object HomeButton : Screen { override val key = "homebutton" }
}

data class LauncherState(
    val loaded: Boolean = false,
    val settings: LauncherSettings = LauncherSettings(),
    val all: List<ResolvedSource> = emptyList(),
    val home: List<ResolvedSource> = emptyList(),
) {
    fun source(id: String?): ResolvedSource? = all.firstOrNull { it.id == id }
}

/** A running auto-select countdown. [token] changes for every new countdown. */
data class Countdown(val targetId: String, val durationMillis: Long, val token: Long)

data class SystemStatus(
    val isDefaultHome: Boolean = false,
    val isHomeServiceEnabled: Boolean = false,
    val hasStockHome: Boolean = false,
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val graph = application.appGraph

    val state: StateFlow<LauncherState> = combine(graph.settings.settings, graph.sources.sources) { settings, sources ->
        val all = SourceResolver.resolve(sources, settings)
        LauncherState(
            loaded = true,
            settings = settings,
            all = all,
            home = SourceResolver.visibleOnHome(all, settings),
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LauncherState())

    private val _backStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val backStack: StateFlow<List<Screen>> = _backStack.asStateFlow()

    private val _countdown = MutableStateFlow<Countdown?>(null)
    val countdown: StateFlow<Countdown?> = _countdown.asStateFlow()

    private val _system = MutableStateFlow(SystemStatus())
    val system: StateFlow<SystemStatus> = _system.asStateFlow()

    /** Incremented whenever Home is pressed, so the home screen can reset its focus. */
    private val _homeVisits = MutableStateFlow(0L)
    val homeVisits: StateFlow<Long> = _homeVisits.asStateFlow()

    /** Last focused item per screen, so going back restores the focus where the user left it. */
    val focusMemory: MutableMap<String, String> = mutableMapOf()

    private var autoSelectArmed = false
    private var countdownJob: Job? = null
    private var countdownToken = 0L

    init {
        viewModelScope.launch {
            state.collect { maybeStartCountdown(it) }
        }
    }

    // region Navigation

    fun navigate(screen: Screen) {
        cancelCountdown()
        _backStack.value = _backStack.value + screen
    }

    /** Returns false when already on the root screen. */
    fun back(): Boolean {
        val stack = _backStack.value
        if (stack.size <= 1) return false
        _backStack.value = stack.dropLast(1)
        return true
    }

    // endregion

    // region Home & auto-select

    /** The launcher was brought up by the Home button (or launched fresh). */
    fun onHomeRequested() {
        _backStack.value = listOf(Screen.Home)
        focusMemory.remove(Screen.Home.key)
        autoSelectArmed = true
        graph.sources.refresh()
        maybeStartCountdown(state.value)
        _homeVisits.value += 1
    }

    fun onUserInteraction() {
        autoSelectArmed = false
        cancelCountdown()
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _countdown.value = null
    }

    private fun maybeStartCountdown(current: LauncherState) {
        if (!autoSelectArmed || !current.loaded) return
        autoSelectArmed = false
        val isOnHome = _backStack.value.lastOrNull() == Screen.Home
        if (!isOnHome) return
        val target = AutoSelectPolicy.target(current.home, current.settings) ?: return
        val durationMillis = current.settings.autoSelectSeconds.coerceAtLeast(1) * 1000L
        countdownJob?.cancel()
        val countdown = Countdown(target.id, durationMillis, ++countdownToken)
        _countdown.value = countdown
        countdownJob = viewModelScope.launch {
            delay(durationMillis)
            val isStillCurrent = _countdown.value == countdown
            _countdown.value = null
            val fresh = state.value.home.firstOrNull { it.id == target.id }
            if (isStillCurrent && fresh != null) switchTo(fresh)
        }
    }

    fun switchTo(source: ResolvedSource) {
        cancelCountdown()
        updateSettings { it.copy(lastUsedSourceId = source.id) }
        graph.switcher.switchTo(getApplication(), source)
    }

    // endregion

    // region System

    fun openTvSettings() = graph.switcher.openTvSettings(getApplication())

    fun openAccessibilitySettings() = graph.switcher.openAccessibilitySettings(getApplication())

    fun openHomeAppSettings() = graph.switcher.openHomeAppSettings(getApplication())

    fun openStockHome(): Boolean {
        HomeGuard.stockHomeAllowed = true
        return graph.switcher.openStockHome(getApplication())
    }

    fun refreshSystemStatus() {
        val app = getApplication<Application>()
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val defaultHome = app.packageManager.resolveActivity(homeIntent, 0)?.activityInfo?.packageName
        val serviceComponent = ComponentName(app, HomeButtonService::class.java)
        val enabledServices = Settings.Secure.getString(app.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        val isServiceEnabled = enabledServices.split(':').any { entry ->
            val component = ComponentName.unflattenFromString(entry)
            component == serviceComponent
        }
        _system.value = SystemStatus(
            isDefaultHome = defaultHome == app.packageName,
            isHomeServiceEnabled = isServiceEnabled,
            hasStockHome = graph.switcher.stockHomeComponent() != null,
        )
        graph.sources.refresh()
    }

    // endregion

    // region Settings

    fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        viewModelScope.launch { graph.settings.update(transform) }
    }

    fun updateOverride(id: String, transform: (SourceOverride) -> SourceOverride) =
        updateSettings { it.withOverride(id, transform) }

    fun rename(id: String, name: String?) = updateOverride(id) { it.copy(name = name?.trim()?.takeIf(String::isNotEmpty)) }

    fun setIcon(id: String, glyph: String?, palette: String?) = updateOverride(id) { it.copy(glyph = glyph, palette = palette) }

    fun setPalette(id: String, palette: String?) = updateOverride(id) { it.copy(palette = palette) }

    fun setHidden(source: ResolvedSource, hidden: Boolean) = updateOverride(source.id) {
        val matchesSystem = hidden == source.source.hiddenBySystem
        it.copy(hidden = if (matchesSystem) null else hidden)
    }

    fun resetSource(id: String) = updateSettings { it.withOverride(id) { SourceOverride() } }

    fun setDefault(id: String?) = updateSettings { it.copy(defaultSourceId = id) }

    fun move(id: String, delta: Int) = updateSettings { settings ->
        // Recompute from the latest saved order so quick repeated moves don't race the UI state.
        val ids = state.value.all.map { it.id }
        val currentOrder = SourceOrder.apply(ids, settings.order) { it }
        settings.copy(order = SourceOrder.move(currentOrder, id, delta))
    }

    // endregion
}
