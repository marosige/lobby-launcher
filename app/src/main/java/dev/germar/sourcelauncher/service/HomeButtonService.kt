package dev.germar.sourcelauncher.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import dev.germar.sourcelauncher.MainActivity
import dev.germar.sourcelauncher.appGraph
import dev.germar.sourcelauncher.model.LauncherSettings
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Optional helper for TVs that don't let a third-party app replace the Home screen (most Google TV
 * devices). As an accessibility service it can see the remote's Home key before the system does:
 *
 * - short press Home: open this launcher;
 * - long press Home: open the TV's own home screen (the system long-press action is lost otherwise);
 * - optionally bring the launcher back whenever the TV's own home screen appears, or when the TV wakes.
 */
class HomeButtonService : AccessibilityService() {
    private val scope = MainScope()

    @Volatile
    private var settings = LauncherSettings()
    private var stockHomePackages: Set<String> = emptySet()
    private var screenOnReceiver: BroadcastReceiver? = null
    private val handler = Handler(Looper.getMainLooper())
    private var homeLongPressed = false

    // Key repeats are synthesised after the accessibility filter, so a held key only ever arrives
    // here as one DOWN and one UP: detect the long press with a timer instead.
    private val longPressAction = Runnable {
        homeLongPressed = true
        openStockHome()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        stockHomePackages = appGraph.switcher.stockHomePackages().toSet()
        scope.launch {
            appGraph.settings.settings.collect {
                settings = it
                updateWakeReceiver(it.showOnWake)
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val isHomeKey = event.keyCode == KeyEvent.KEYCODE_HOME
        if (!isHomeKey || !settings.interceptHomeButton) return false
        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                val isFirstDown = event.repeatCount == 0
                if (isFirstDown) {
                    homeLongPressed = false
                    handler.removeCallbacks(longPressAction)
                    handler.postDelayed(longPressAction, LONG_PRESS_MILLIS)
                }
            }
            KeyEvent.ACTION_UP -> {
                handler.removeCallbacks(longPressAction)
                if (!homeLongPressed) {
                    HomeGuard.stockHomeAllowed = false
                    showLauncher()
                }
            }
        }
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val isWindowChange = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        if (!isWindowChange || !settings.replaceStockHome) return
        val pkg = event.packageName?.toString() ?: return
        val isStockHome = pkg in stockHomePackages
        val userWantsStockHome = HomeGuard.stockHomeAllowed
        if (isStockHome && !userWantsStockHome) showLauncher()
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        handler.removeCallbacks(longPressAction)
        updateWakeReceiver(false)
        scope.cancel()
        super.onDestroy()
    }

    private fun showLauncher() {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            .putExtra(MainActivity.EXTRA_FROM_HOME_BUTTON, true)
        runCatching { startActivity(intent) }
    }

    private fun openStockHome() {
        HomeGuard.stockHomeAllowed = true
        val opened = appGraph.switcher.openStockHome(this)
        if (!opened) performGlobalAction(GLOBAL_ACTION_HOME)
    }

    private fun updateWakeReceiver(enabled: Boolean) {
        val isRegistered = screenOnReceiver != null
        if (enabled && !isRegistered) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    HomeGuard.stockHomeAllowed = false
                    showLauncher()
                }
            }
            ContextCompat.registerReceiver(this, receiver, IntentFilter(Intent.ACTION_SCREEN_ON), ContextCompat.RECEIVER_NOT_EXPORTED)
            screenOnReceiver = receiver
        } else if (!enabled && isRegistered) {
            runCatching { unregisterReceiver(screenOnReceiver) }
            screenOnReceiver = null
        }
    }
}

private val LONG_PRESS_MILLIS = ViewConfiguration.getLongPressTimeout().toLong().coerceAtLeast(500L)

/** Lets the user deliberately open the TV's own home screen while "replace stock home" is on. */
object HomeGuard {
    @Volatile
    var stockHomeAllowed: Boolean = false
}
