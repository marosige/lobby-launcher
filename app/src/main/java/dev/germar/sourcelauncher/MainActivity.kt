package dev.germar.sourcelauncher

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dev.germar.sourcelauncher.service.HomeGuard
import dev.germar.sourcelauncher.ui.LauncherApp
import dev.germar.sourcelauncher.ui.LauncherViewModel
import dev.germar.sourcelauncher.ui.theme.LauncherTheme

class MainActivity : ComponentActivity() {
    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LauncherTheme {
                LauncherApp(viewModel = viewModel, onExit = ::onExitRequested)
            }
        }
        if (savedInstanceState == null) handleLaunch(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunch(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshSystemStatus()
    }

    override fun onStop() {
        super.onStop()
        viewModel.cancelCountdown()
    }

    // Overriding is public API; lint misreads ComponentActivity's @RestrictTo override as a restricted call.
    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Any button on the remote stops the auto-select countdown; the key itself still works normally.
        if (event.action == KeyEvent.ACTION_DOWN) viewModel.onUserInteraction()
        return super.dispatchKeyEvent(event)
    }

    private fun handleLaunch(intent: Intent) {
        if (intent.hasExtra(EXTRA_DEMO)) {
            val demo = intent.getBooleanExtra(EXTRA_DEMO, false)
            viewModel.updateSettings { it.copy(demoMode = demo) }
        }
        val isHomeLaunch = isHomeLaunch(intent)
        if (isHomeLaunch) HomeGuard.stockHomeAllowed = false
        viewModel.onHomeRequested()
    }

    /** Back on the home screen: a real launcher stays put, otherwise return to wherever we came from. */
    private fun onExitRequested() {
        if (!isHomeLaunch(intent)) finish()
    }

    private fun isHomeLaunch(intent: Intent?): Boolean {
        val hasHomeCategory = intent?.hasCategory(Intent.CATEGORY_HOME) == true
        val fromHomeButton = intent?.getBooleanExtra(EXTRA_FROM_HOME_BUTTON, false) == true
        return hasHomeCategory || fromHomeButton
    }

    companion object {
        const val EXTRA_FROM_HOME_BUTTON = "dev.germar.sourcelauncher.extra.FROM_HOME_BUTTON"

        /** `adb shell am start -n dev.germar.sourcelauncher/.MainActivity --ez demo true` */
        const val EXTRA_DEMO = "demo"
    }
}
