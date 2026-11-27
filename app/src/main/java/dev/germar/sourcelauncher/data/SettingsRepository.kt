package dev.germar.sourcelauncher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import dev.germar.sourcelauncher.model.LauncherSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/** Unknown keys and values (from newer or older versions) fall back to defaults field by field. */
internal val settingsJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = false
}

private object LauncherSettingsSerializer : Serializer<LauncherSettings> {
    override val defaultValue: LauncherSettings = LauncherSettings()

    override suspend fun readFrom(input: InputStream): LauncherSettings = try {
        settingsJson.decodeFromString(LauncherSettings.serializer(), input.readBytes().decodeToString())
    } catch (_: SerializationException) {
        defaultValue
    } catch (_: IllegalArgumentException) {
        defaultValue
    }

    override suspend fun writeTo(t: LauncherSettings, output: OutputStream) {
        output.write(settingsJson.encodeToString(LauncherSettings.serializer(), t).encodeToByteArray())
    }
}

private val Context.settingsStore: DataStore<LauncherSettings> by dataStore(
    fileName = "launcher_settings.json",
    serializer = LauncherSettingsSerializer,
)

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsStore

    val settings: Flow<LauncherSettings> = store.data

    suspend fun update(transform: (LauncherSettings) -> LauncherSettings) {
        store.updateData(transform)
    }
}
