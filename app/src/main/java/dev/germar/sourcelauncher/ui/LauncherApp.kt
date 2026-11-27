package dev.germar.sourcelauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.germar.sourcelauncher.ui.components.ScreenBackground
import dev.germar.sourcelauncher.ui.home.HomeScreen
import dev.germar.sourcelauncher.ui.settings.ClockPickerScreen
import dev.germar.sourcelauncher.ui.settings.ColorPickerScreen
import dev.germar.sourcelauncher.ui.settings.DefaultPickerScreen
import dev.germar.sourcelauncher.ui.settings.DelayPickerScreen
import dev.germar.sourcelauncher.ui.settings.HomeButtonScreen
import dev.germar.sourcelauncher.ui.settings.IconPickerScreen
import dev.germar.sourcelauncher.ui.settings.RenameScreen
import dev.germar.sourcelauncher.ui.settings.ReorderScreen
import dev.germar.sourcelauncher.ui.settings.SettingsScreen
import dev.germar.sourcelauncher.ui.settings.SourceEditScreen

@Composable
fun LauncherApp(viewModel: LauncherViewModel, onExit: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStack by viewModel.backStack.collectAsStateWithLifecycle()
    val countdown by viewModel.countdown.collectAsStateWithLifecycle()
    val system by viewModel.system.collectAsStateWithLifecycle()
    val homeVisits by viewModel.homeVisits.collectAsStateWithLifecycle()
    val screen = backStack.last()

    BackHandler {
        val wentBack = viewModel.back()
        if (!wentBack) {
            viewModel.cancelCountdown()
            onExit()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(ScreenBackground)) {
        key(screen.key) {
            val alpha = remember { Animatable(0f) }
            LaunchedEffect(Unit) { alpha.animateTo(1f, tween(220)) }
            Box(modifier = Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha.value }) {
                when (screen) {
                    Screen.Home -> HomeScreen(
                        state = state,
                        countdown = countdown,
                        system = system,
                        homeVisit = homeVisits,
                        focusMemory = viewModel.focusMemory,
                        onSwitch = viewModel::switchTo,
                        onOpenSettings = { viewModel.navigate(Screen.Settings) },
                        onOpenTvSettings = viewModel::openTvSettings,
                        onOpenStockHome = { viewModel.openStockHome() },
                        onEnableDemo = { viewModel.updateSettings { it.copy(demoMode = true) } },
                    )
                    Screen.Settings -> SettingsScreen(viewModel, state, system)
                    is Screen.SourceEdit -> SourceEditScreen(viewModel, state, screen.id)
                    is Screen.Rename -> RenameScreen(viewModel, state, screen.id)
                    is Screen.IconPicker -> IconPickerScreen(viewModel, state, screen.id)
                    is Screen.ColorPicker -> ColorPickerScreen(viewModel, state, screen.id)
                    Screen.Reorder -> ReorderScreen(viewModel, state)
                    Screen.DefaultPicker -> DefaultPickerScreen(viewModel, state)
                    Screen.DelayPicker -> DelayPickerScreen(viewModel, state)
                    Screen.ClockPicker -> ClockPickerScreen(viewModel, state)
                    Screen.HomeButton -> HomeButtonScreen(viewModel, state, system)
                }
            }
        }
    }
}
