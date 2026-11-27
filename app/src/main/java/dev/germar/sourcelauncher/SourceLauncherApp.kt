package dev.germar.sourcelauncher

import android.app.Application
import android.content.Context
import dev.germar.sourcelauncher.data.InputSwitcher
import dev.germar.sourcelauncher.data.SettingsRepository
import dev.germar.sourcelauncher.data.SourceRepository

class SourceLauncherApp : Application() {
    val graph: AppGraph by lazy { AppGraph(this) }
}

class AppGraph(context: Context) {
    val settings = SettingsRepository(context)
    val sources = SourceRepository(context, settings)
    val switcher = InputSwitcher(context)
}

val Context.appGraph: AppGraph get() = (applicationContext as SourceLauncherApp).graph
