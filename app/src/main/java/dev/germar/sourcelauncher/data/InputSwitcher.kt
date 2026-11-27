package dev.germar.sourcelauncher.data

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.tv.TvContract
import android.provider.Settings
import android.widget.Toast
import dev.germar.sourcelauncher.R
import dev.germar.sourcelauncher.model.ResolvedSource

/** Switches the TV to an input and opens system screens. */
class InputSwitcher(context: Context) {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager

    /** Returns true when the switch request was handed to the system. */
    fun switchTo(context: Context, source: ResolvedSource): Boolean {
        if (DemoSources.isDemo(source.id)) {
            toast(context, context.getString(R.string.demo_switch_toast, source.name))
            return true
        }
        val switched = if (source.source.isPassthrough) {
            startFirst(context, passthroughIntent(source.id)) || startLiveTvApp(context)
        } else {
            startLiveTvApp(context)
        }
        if (!switched) toast(context, context.getString(R.string.switch_failed_toast, source.name))
        return switched
    }

    fun openTvSettings(context: Context) {
        startFirst(context, Intent(Settings.ACTION_SETTINGS))
    }

    fun openAccessibilitySettings(context: Context) {
        startFirst(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), Intent(Settings.ACTION_SETTINGS))
    }

    fun openHomeAppSettings(context: Context) {
        startFirst(context, Intent(Settings.ACTION_HOME_SETTINGS), Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS), Intent(Settings.ACTION_SETTINGS))
    }

    /** The TV's own launcher (e.g. Google TV home), if another home app is installed. */
    fun stockHomeComponent(): ComponentName? = stockHomePackages().firstNotNullOfOrNull { pkg ->
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setPackage(pkg)
        packageManager.resolveActivity(homeIntent, 0)?.activityInfo?.let { ComponentName(it.packageName, it.name) }
    }

    fun openStockHome(context: Context): Boolean {
        val component = stockHomeComponent() ?: return false
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .setComponent(component)
        return startFirst(context, intent)
    }

    fun stockHomePackages(): List<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val liveTvPackage = packageManager.resolveActivity(passthroughIntent("probe"), 0)?.activityInfo?.packageName
        return packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .map { it.activityInfo.packageName }
            .filter { pkg ->
                val isSelf = pkg == appContext.packageName
                val isSetupOrSettings = pkg.contains("setup", ignoreCase = true) || pkg.contains("settings", ignoreCase = true)
                // Never treat the app that shows HDMI inputs as "home", or switching would bounce back here.
                val isLiveTvApp = pkg == liveTvPackage
                !isSelf && !isSetupOrSettings && !isLiveTvApp
            }
            .distinct()
    }

    private fun passthroughIntent(inputId: String): Intent =
        Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(inputId))

    /** Opens the TV's Live TV / input app, which is what handles input switching on most TVs. */
    private fun startLiveTvApp(context: Context): Boolean {
        val probe = passthroughIntent("probe")
        val handlerPackage = packageManager.resolveActivity(probe, 0)?.activityInfo?.packageName
        val candidates = listOfNotNull(handlerPackage) + KNOWN_LIVE_TV_PACKAGES
        val launchIntents = candidates.distinct().mapNotNull { pkg ->
            packageManager.getLeanbackLaunchIntentForPackage(pkg) ?: packageManager.getLaunchIntentForPackage(pkg)
        }
        return startFirst(context, *launchIntents.toTypedArray())
    }

    private fun startFirst(context: Context, vararg intents: Intent): Boolean {
        for (intent in intents) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return true
            } catch (_: ActivityNotFoundException) {
            } catch (_: SecurityException) {
            }
        }
        return false
    }

    private fun toast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        val KNOWN_LIVE_TV_PACKAGES = listOf(
            "com.google.android.tv",
            "com.android.tv",
            "com.sony.dtv.tvx",
            "com.mediatek.wwtv.tvcenter",
            "com.droidlogic.tvsource",
            "org.droidtv.playtv",
        )
    }
}
