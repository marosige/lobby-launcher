package dev.germar.sourcelauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import dev.germar.sourcelauncher.R
import dev.germar.sourcelauncher.model.ClockFormat
import dev.germar.sourcelauncher.model.ConnectionState
import dev.germar.sourcelauncher.model.LauncherSettings
import dev.germar.sourcelauncher.model.Palette
import dev.germar.sourcelauncher.model.ResolvedSource
import dev.germar.sourcelauncher.ui.LauncherState
import dev.germar.sourcelauncher.ui.LauncherViewModel
import dev.germar.sourcelauncher.ui.Screen
import dev.germar.sourcelauncher.ui.SystemStatus
import dev.germar.sourcelauncher.ui.components.CheckRow
import dev.germar.sourcelauncher.ui.components.RowAvatar
import dev.germar.sourcelauncher.ui.components.SectionHeader
import dev.germar.sourcelauncher.ui.components.SettingsRow
import dev.germar.sourcelauncher.ui.components.ToggleRow
import dev.germar.sourcelauncher.ui.components.TwoPaneScreen
import dev.germar.sourcelauncher.ui.components.clockFormatter
import dev.germar.sourcelauncher.ui.components.rememberFocusPlan
import dev.germar.sourcelauncher.ui.icons.SourceAvatar
import dev.germar.sourcelauncher.ui.icons.bottomColor
import dev.germar.sourcelauncher.ui.icons.topColor
import dev.germar.sourcelauncher.ui.theme.LauncherColors
import java.util.Date

// region Main settings

@Composable
fun SettingsScreen(vm: LauncherViewModel, state: LauncherState, system: SystemStatus) {
    val settings = state.settings
    val defaultSource = state.source(settings.defaultSourceId)
    val sourceKeys = state.all.map { "source:${it.id}" }
    val keys = sourceKeys + listOf(
        "reorder", "auto", "default", "delay", "connected", "disconnected", "tuner", "clock",
        "homebutton", "icons", "demo", "tvsettings", "version",
    )
    val plan = rememberFocusPlan(Screen.Settings.key, vm.focusMemory, keys)
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
    }

    TwoPaneScreen(
        title = stringResource(R.string.settings_title),
        description = stringResource(R.string.settings_description),
        hero = { HeroIcon(Icons.Filled.Settings, Palette.GRAPHITE) },
    ) {
        SectionHeader(stringResource(R.string.section_sources))
        if (state.all.isEmpty()) {
            SettingsRow(
                title = stringResource(R.string.empty_title),
                subtitle = stringResource(R.string.empty_settings_hint),
                onClick = {},
                leading = { Icon(Icons.Filled.Warning, contentDescription = null) },
            )
        }
        state.all.forEach { source ->
            SettingsRow(
                title = source.name,
                subtitle = sourceSummary(source),
                showChevron = true,
                leading = { RowAvatar(source.appearance, source.name, dimmed = source.hidden) },
                onClick = { vm.navigate(Screen.SourceEdit(source.id)) },
                modifier = plan.item("source:${source.id}"),
            )
        }
        SettingsRow(
            title = stringResource(R.string.settings_reorder),
            subtitle = stringResource(R.string.settings_reorder_sub),
            showChevron = true,
            onClick = { vm.navigate(Screen.Reorder) },
            modifier = plan.item("reorder"),
        )

        SectionHeader(stringResource(R.string.section_auto))
        ToggleRow(
            title = stringResource(R.string.auto_enabled),
            subtitle = stringResource(R.string.auto_enabled_sub),
            checked = settings.autoSelectEnabled,
            onCheckedChange = { enabled -> vm.updateSettings { it.withAutoSelect(enabled, state) } },
            modifier = plan.item("auto"),
        )
        SettingsRow(
            title = stringResource(R.string.auto_default),
            value = defaultSource?.name ?: stringResource(R.string.none),
            showChevron = true,
            onClick = { vm.navigate(Screen.DefaultPicker) },
            modifier = plan.item("default"),
        )
        SettingsRow(
            title = stringResource(R.string.auto_delay),
            value = stringResource(R.string.seconds_value, settings.autoSelectSeconds),
            showChevron = true,
            onClick = { vm.navigate(Screen.DelayPicker) },
            modifier = plan.item("delay"),
        )
        ToggleRow(
            title = stringResource(R.string.auto_only_connected),
            subtitle = stringResource(R.string.auto_only_connected_sub),
            checked = settings.autoSelectOnlyWhenConnected,
            onCheckedChange = { checked -> vm.updateSettings { it.copy(autoSelectOnlyWhenConnected = checked) } },
            modifier = plan.item("connected"),
        )

        SectionHeader(stringResource(R.string.section_home))
        ToggleRow(
            title = stringResource(R.string.show_disconnected),
            subtitle = stringResource(R.string.show_disconnected_sub),
            checked = settings.showDisconnected,
            onCheckedChange = { checked -> vm.updateSettings { it.copy(showDisconnected = checked) } },
            modifier = plan.item("disconnected"),
        )
        ToggleRow(
            title = stringResource(R.string.show_tuner),
            subtitle = stringResource(R.string.show_tuner_sub),
            checked = settings.showTunerInputs,
            onCheckedChange = { checked -> vm.updateSettings { it.copy(showTunerInputs = checked) } },
            modifier = plan.item("tuner"),
        )
        SettingsRow(
            title = stringResource(R.string.clock_format),
            value = clockFormatName(settings.clockFormat),
            showChevron = true,
            onClick = { vm.navigate(Screen.ClockPicker) },
            modifier = plan.item("clock"),
        )
        val isHomeButtonSetUp = system.isDefaultHome || system.isHomeServiceEnabled
        SettingsRow(
            title = stringResource(R.string.home_button),
            value = stringResource(if (isHomeButtonSetUp) R.string.home_button_active else R.string.home_button_inactive),
            showChevron = true,
            onClick = { vm.navigate(Screen.HomeButton) },
            modifier = plan.item("homebutton"),
        )

        SectionHeader(stringResource(R.string.section_more))
        SettingsRow(
            title = stringResource(R.string.icon_pack),
            subtitle = stringResource(R.string.icon_pack_sub),
            showChevron = true,
            onClick = { vm.navigate(Screen.IconPicker(null)) },
            modifier = plan.item("icons"),
        )
        ToggleRow(
            title = stringResource(R.string.demo_mode),
            subtitle = stringResource(R.string.demo_mode_sub),
            checked = settings.demoMode,
            onCheckedChange = { checked -> vm.updateSettings { it.copy(demoMode = checked) } },
            modifier = plan.item("demo"),
        )
        SettingsRow(
            title = stringResource(R.string.action_tv_settings),
            subtitle = stringResource(R.string.tv_settings_sub),
            showChevron = true,
            onClick = { vm.openTvSettings() },
            modifier = plan.item("tvsettings"),
        )
        SettingsRow(
            title = stringResource(R.string.version),
            value = version,
            onClick = {},
            modifier = plan.item("version"),
        )
    }
}

/** Turning auto-select on without a default picks the most sensible one, so it works right away. */
private fun LauncherSettings.withAutoSelect(enabled: Boolean, state: LauncherState): LauncherSettings {
    val hasDefault = state.home.any { it.id == defaultSourceId }
    if (!enabled || hasDefault) return copy(autoSelectEnabled = enabled)
    val lastUsed = state.home.firstOrNull { it.id == lastUsedSourceId }
    val firstConnected = state.home.firstOrNull { !it.isDisconnected }
    val suggested = lastUsed ?: firstConnected ?: state.home.firstOrNull()
    return copy(autoSelectEnabled = true, defaultSourceId = suggested?.id ?: defaultSourceId)
}

@Composable
private fun sourceSummary(source: ResolvedSource): String {
    val parts = buildList {
        add(source.source.portLabel)
        add(statusText(source.connection))
        if (source.hidden) add(stringResource(R.string.source_hidden))
    }
    return parts.filter { it.isNotBlank() }.distinct().joinToString(" · ")
}

@Composable
private fun statusText(connection: ConnectionState): String = stringResource(
    when (connection) {
        ConnectionState.CONNECTED -> R.string.status_connected
        ConnectionState.STANDBY -> R.string.status_standby
        ConnectionState.DISCONNECTED -> R.string.status_disconnected
        ConnectionState.UNKNOWN -> R.string.status_unknown
    },
)

// endregion

// region Source editor

@Composable
fun SourceEditScreen(vm: LauncherViewModel, state: LauncherState, id: String) {
    val source = state.source(id)
    if (source == null) {
        MissingSource()
        return
    }
    val keys = listOf("name", "icon", "color", "show", "reset", "input", "detected", "reported", "status")
    val plan = rememberFocusPlan("edit:$id", vm.focusMemory, keys)
    val automatic = stringResource(R.string.automatic)
    val custom = stringResource(R.string.custom)

    TwoPaneScreen(
        title = source.name,
        description = source.portLabel,
        hero = { SourceAvatar(source.appearance, source.name, 150.dp, dimmed = source.isDisconnected) },
    ) {
        SettingsRow(
            title = stringResource(R.string.edit_name),
            value = if (source.hasCustomName) source.name else "$automatic · ${source.name}",
            showChevron = true,
            onClick = { vm.navigate(Screen.Rename(id)) },
            modifier = plan.item("name"),
        )
        SettingsRow(
            title = stringResource(R.string.edit_icon),
            value = if (source.hasCustomIcon) custom else automatic,
            showChevron = true,
            leading = { RowAvatar(source.appearance, source.name) },
            onClick = { vm.navigate(Screen.IconPicker(id)) },
            modifier = plan.item("icon"),
        )
        val hasCustomPalette = state.settings.override(id).palette != null
        SettingsRow(
            title = stringResource(R.string.edit_color),
            value = if (hasCustomPalette) custom else automatic,
            showChevron = true,
            leading = { PaletteDot(source.appearance.palette) },
            onClick = { vm.navigate(Screen.ColorPicker(id)) },
            modifier = plan.item("color"),
        )
        ToggleRow(
            title = stringResource(R.string.edit_show_on_home),
            checked = !source.hidden,
            onCheckedChange = { visible -> vm.setHidden(source, hidden = !visible) },
            modifier = plan.item("show"),
        )
        SettingsRow(
            title = stringResource(R.string.edit_reset),
            subtitle = stringResource(R.string.edit_reset_sub, source.detectedName),
            onClick = {
                vm.resetSource(id)
                plan.focus("name")
            },
            modifier = plan.item("reset"),
        )

        SectionHeader(stringResource(R.string.section_info))
        SettingsRow(
            title = stringResource(R.string.edit_input),
            value = source.source.portLabel,
            onClick = {},
            modifier = plan.item("input"),
        )
        SettingsRow(
            title = stringResource(R.string.edit_detected),
            value = source.profile?.name ?: stringResource(R.string.not_recognised),
            onClick = {},
            modifier = plan.item("detected"),
        )
        val reported = (listOfNotNull(source.source.customLabel) + source.source.deviceLabels).distinct()
        if (reported.isNotEmpty()) {
            SettingsRow(
                title = stringResource(R.string.edit_reported),
                value = reported.joinToString(", "),
                onClick = {},
                modifier = plan.item("reported"),
            )
        }
        SettingsRow(
            title = stringResource(R.string.edit_status),
            value = statusText(source.connection),
            onClick = {},
            modifier = plan.item("status"),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.edit_hint),
            style = androidx.tv.material3.MaterialTheme.typography.bodyMedium,
            color = LauncherColors.TextTertiary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun MissingSource() {
    TwoPaneScreen(
        title = stringResource(R.string.edit_missing),
        hero = { HeroIcon(Icons.Filled.Warning, Palette.GRAPHITE) },
    ) {}
}

@Composable
private fun PaletteDot(palette: Palette) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(palette.topColor, palette.bottomColor))),
    )
}

// endregion

// region Rename

@Composable
fun RenameScreen(vm: LauncherViewModel, state: LauncherState, id: String) {
    val source = state.source(id)
    if (source == null) {
        MissingSource()
        return
    }
    var text by remember {
        mutableStateOf(TextFieldValue(source.name, selection = TextRange(source.name.length)))
    }
    val save: () -> Unit = {
        val trimmed = text.text.trim()
        val isAutomatic = trimmed.isEmpty() || trimmed == source.detectedName
        vm.rename(id, if (isAutomatic) null else trimmed)
        vm.back()
    }
    val suggestions = buildList {
        add(source.detectedName)
        source.profile?.name?.let(::add)
        source.source.customLabel?.let(::add)
        addAll(source.source.deviceLabels)
        add(source.source.portLabel)
    }.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    val keys = listOf("field", "save") + suggestions.map { "suggestion:$it" }
    val plan = rememberFocusPlan("rename:$id", vm.focusMemory, keys)

    TwoPaneScreen(
        title = stringResource(R.string.rename_title),
        description = stringResource(R.string.rename_description),
        hero = { SourceAvatar(source.appearance, text.text.ifBlank { source.detectedName }, 150.dp) },
    ) {
        NameField(
            value = text,
            onValueChange = { text = it },
            onDone = save,
            modifier = plan.item("field"),
        )
        Spacer(Modifier.height(8.dp))
        SettingsRow(title = stringResource(R.string.rename_save), onClick = save, modifier = plan.item("save"))
        SectionHeader(stringResource(R.string.rename_suggestions))
        suggestions.forEach { suggestion ->
            val isDetected = suggestion == source.detectedName
            SettingsRow(
                title = suggestion,
                value = if (isDetected) stringResource(R.string.automatic) else null,
                onClick = {
                    vm.rename(id, if (isDetected) null else suggestion)
                    vm.back()
                },
                modifier = plan.item("suggestion:$suggestion"),
            )
        }
    }
}

@Composable
private fun NameField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(14.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        // Compose only leaves a text field on D-pad events from physical devices; remotes that inject
        // through a virtual device (phone remote apps, adb) would otherwise be stuck in the field.
        modifier = modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { event ->
                val isDownPress = event.key == Key.DirectionDown && event.type == KeyEventType.KeyDown
                if (isDownPress) focusManager.moveFocus(FocusDirection.Down) else false
            },
        singleLine = true,
        interactionSource = interactionSource,
        textStyle = TextStyle(color = LauncherColors.TextPrimary, fontSize = 22.sp),
        cursorBrush = SolidColor(LauncherColors.TextPrimary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(Color.White.copy(alpha = if (focused) 0.16f else 0.08f))
                    .border(2.dp, if (focused) Color.White else Color.Transparent, shape)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) { inner() }
        },
    )
}

// endregion

// region Default source & delay

@Composable
fun DefaultPickerScreen(vm: LauncherViewModel, state: LauncherState) {
    val candidates = state.all.filter { !it.hidden }
    val current = state.settings.defaultSourceId?.takeIf { id -> candidates.any { it.id == id } }
    val keys = listOf("none") + candidates.map { it.id }
    val plan = rememberFocusPlan(Screen.DefaultPicker.key, vm.focusMemory, keys, fallback = current ?: "none")
    val heroSource = state.source(current)

    TwoPaneScreen(
        title = stringResource(R.string.auto_default),
        description = stringResource(R.string.default_description),
        hero = {
            if (heroSource != null) SourceAvatar(heroSource.appearance, heroSource.name, 150.dp)
            else HeroIcon(Icons.Filled.Home, Palette.GRAPHITE)
        },
    ) {
        CheckRow(
            title = stringResource(R.string.none),
            checked = current == null,
            onClick = {
                vm.setDefault(null)
                vm.back()
            },
            modifier = plan.item("none"),
        )
        candidates.forEach { source ->
            CheckRow(
                title = source.name,
                subtitle = source.portLabel,
                checked = source.id == current,
                leading = { RowAvatar(source.appearance, source.name) },
                onClick = {
                    vm.setDefault(source.id)
                    vm.back()
                },
                modifier = plan.item(source.id),
            )
        }
    }
}

@Composable
fun DelayPickerScreen(vm: LauncherViewModel, state: LauncherState) {
    val current = state.settings.autoSelectSeconds
    val choices = (LauncherSettings.AUTO_SELECT_CHOICES + current).distinct().sorted()
    val keys = choices.map { it.toString() }
    val plan = rememberFocusPlan(Screen.DelayPicker.key, vm.focusMemory, keys, fallback = current.toString())

    TwoPaneScreen(
        title = stringResource(R.string.auto_delay),
        description = stringResource(R.string.delay_description),
        hero = { HeroText(stringResource(R.string.seconds_value, current)) },
    ) {
        choices.forEach { seconds ->
            CheckRow(
                title = stringResource(R.string.seconds_value, seconds),
                checked = seconds == current,
                onClick = {
                    vm.updateSettings { it.copy(autoSelectSeconds = seconds) }
                    vm.back()
                },
                modifier = plan.item(seconds.toString()),
            )
        }
    }
}

@Composable
fun ClockPickerScreen(vm: LauncherViewModel, state: LauncherState) {
    val current = state.settings.clockFormat
    val keys = ClockFormat.entries.map { it.name }
    val plan = rememberFocusPlan(Screen.ClockPicker.key, vm.focusMemory, keys, fallback = current.name)
    val context = LocalContext.current
    val now = remember { Date() }

    TwoPaneScreen(
        title = stringResource(R.string.clock_format),
        description = stringResource(R.string.clock_description),
        hero = { HeroText(clockFormatter(context, current).format(now), fontSize = 30.sp) },
    ) {
        ClockFormat.entries.forEach { format ->
            CheckRow(
                title = clockFormatName(format),
                subtitle = clockFormatter(context, format).format(now),
                checked = format == current,
                onClick = {
                    vm.updateSettings { it.copy(clockFormat = format) }
                    vm.back()
                },
                modifier = plan.item(format.name),
            )
        }
    }
}

@Composable
private fun clockFormatName(format: ClockFormat): String = stringResource(
    when (format) {
        ClockFormat.SYSTEM -> R.string.clock_system
        ClockFormat.H12 -> R.string.clock_12h
        ClockFormat.H24 -> R.string.clock_24h
    },
)

// endregion

// region Home button

@Composable
fun HomeButtonScreen(vm: LauncherViewModel, state: LauncherState, system: SystemStatus) {
    val settings = state.settings
    val keys = listOf("defaulthome", "service", "intercept", "wake", "replace", "adb_home", "adb_service")
    val plan = rememberFocusPlan(Screen.HomeButton.key, vm.focusMemory, keys)
    val needsService = !system.isHomeServiceEnabled
    val requiresService = stringResource(R.string.hb_requires_service)

    TwoPaneScreen(
        title = stringResource(R.string.home_button),
        description = stringResource(R.string.hb_description),
        hero = { HeroIcon(Icons.Filled.Home, Palette.GRAPHITE) },
    ) {
        SettingsRow(
            title = stringResource(R.string.hb_default_home),
            subtitle = stringResource(R.string.hb_default_home_sub),
            value = stringResource(if (system.isDefaultHome) R.string.hb_default_home_yes else R.string.hb_default_home_no),
            showChevron = true,
            onClick = { vm.openHomeAppSettings() },
            modifier = plan.item("defaulthome"),
        )
        SettingsRow(
            title = stringResource(R.string.hb_service),
            subtitle = stringResource(R.string.hb_service_sub),
            value = stringResource(if (system.isHomeServiceEnabled) R.string.on else R.string.off),
            showChevron = true,
            onClick = { vm.openAccessibilitySettings() },
            modifier = plan.item("service"),
        )
        ToggleRow(
            title = stringResource(R.string.hb_intercept),
            subtitle = if (needsService) requiresService else stringResource(R.string.hb_intercept_sub),
            checked = settings.interceptHomeButton,
            onCheckedChange = { checked -> vm.updateSettings { it.copy(interceptHomeButton = checked) } },
            modifier = plan.item("intercept"),
        )
        ToggleRow(
            title = stringResource(R.string.hb_wake),
            subtitle = if (needsService) requiresService else stringResource(R.string.hb_wake_sub),
            checked = settings.showOnWake,
            onCheckedChange = { checked -> vm.updateSettings { it.copy(showOnWake = checked) } },
            modifier = plan.item("wake"),
        )
        ToggleRow(
            title = stringResource(R.string.hb_replace),
            subtitle = if (needsService) requiresService else stringResource(R.string.hb_replace_sub),
            checked = settings.replaceStockHome,
            onCheckedChange = { checked -> vm.updateSettings { it.copy(replaceStockHome = checked) } },
            modifier = plan.item("replace"),
        )
        SectionHeader(stringResource(R.string.hb_adb_title))
        SettingsRow(
            title = stringResource(R.string.hb_adb_home),
            subtitle = "adb shell cmd package set-home-activity dev.germar.sourcelauncher/.MainActivity",
            onClick = {},
            modifier = plan.item("adb_home"),
        )
        SettingsRow(
            title = stringResource(R.string.hb_adb_service),
            subtitle = "adb shell settings put secure enabled_accessibility_services dev.germar.sourcelauncher/.service.HomeButtonService",
            onClick = {},
            modifier = plan.item("adb_service"),
        )
    }
}

// endregion

// region Heroes

@Composable
internal fun HeroIcon(icon: ImageVector, palette: Palette) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(palette.topColor, palette.bottomColor))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(72.dp))
    }
}

@Composable
private fun HeroText(text: String, fontSize: TextUnit = 40.sp) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Palette.GRAPHITE.topColor, Palette.GRAPHITE.bottomColor))),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color.White, fontSize = fontSize)
    }
}

// endregion
