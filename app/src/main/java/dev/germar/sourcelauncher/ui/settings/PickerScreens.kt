package dev.germar.sourcelauncher.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import dev.germar.sourcelauncher.R
import dev.germar.sourcelauncher.catalog.DeviceCatalog
import dev.germar.sourcelauncher.catalog.DeviceCategory
import dev.germar.sourcelauncher.catalog.IconPreset
import dev.germar.sourcelauncher.model.Appearance
import dev.germar.sourcelauncher.model.Palette
import dev.germar.sourcelauncher.ui.LauncherState
import dev.germar.sourcelauncher.ui.LauncherViewModel
import dev.germar.sourcelauncher.ui.Screen
import dev.germar.sourcelauncher.ui.components.AvatarTile
import dev.germar.sourcelauncher.ui.components.PillButton
import dev.germar.sourcelauncher.ui.components.SectionHeader
import dev.germar.sourcelauncher.ui.components.rememberFocusPlan
import dev.germar.sourcelauncher.ui.icons.SourceAvatar
import dev.germar.sourcelauncher.ui.theme.LauncherColors

private const val KEY_AUTOMATIC = "automatic"

// region Grid scaffolding

@Composable
private fun GridScreen(
    title: String,
    description: String,
    preview: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 56.dp, vertical = 40.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (preview != null) {
                preview()
                Spacer(Modifier.width(24.dp))
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.headlineMedium, color = LauncherColors.TextPrimary)
                Text(text = description, style = MaterialTheme.typography.bodyLarge, color = LauncherColors.TextSecondary)
            }
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TileGrid(content: @Composable () -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) { content() }
}

@Composable
private fun categoryName(category: DeviceCategory): String = stringResource(
    when (category) {
        DeviceCategory.CONSOLE -> R.string.category_console
        DeviceCategory.HANDHELD -> R.string.category_handheld
        DeviceCategory.STREAMING -> R.string.category_streaming
        DeviceCategory.TV_SERVICE -> R.string.category_tv_service
        DeviceCategory.MEDIA -> R.string.category_media
        DeviceCategory.AUDIO -> R.string.category_audio
        DeviceCategory.COMPUTER -> R.string.category_computer
        DeviceCategory.OTHER -> R.string.category_other
    },
)

// endregion

// region Icon picker / icon pack gallery

/** A name offered after picking an icon; [clearsCustomName] when it is the name the TV detected. */
private data class NamePrompt(val appearance: Appearance, val name: String, val clearsCustomName: Boolean)

/**
 * Picks an icon for source [id], or shows the whole icon pack when [id] is null. After a pick, offers
 * the device's name so it does not have to be typed with the remote.
 */
@Composable
fun IconPickerScreen(vm: LauncherViewModel, state: LauncherState, id: String?) {
    val source = id?.let(state::source)
    val isGallery = source == null
    val presets = DeviceCatalog.presets
    val selectedKey = when {
        source == null -> null
        !source.hasCustomIcon -> KEY_AUTOMATIC
        else -> {
            val sameIcon = presets.filter { it.glyph == source.appearance.glyph && it.palette == source.appearance.palette }
            val sameIconAndName = sameIcon.firstOrNull { it.suggestedName.equals(source.name, ignoreCase = true) }
            (sameIconAndName ?: sameIcon.firstOrNull())?.id
        }
    }
    val keys = (if (isGallery) emptyList() else listOf(KEY_AUTOMATIC)) + presets.map { it.id }
    val plan = rememberFocusPlan(Screen.IconPicker(id).key, vm.focusMemory, keys, fallback = selectedKey)
    val tileSize = 72.dp
    val labelWidth = 128.dp
    val labelStyle = MaterialTheme.typography.bodyMedium
    var namePrompt by remember { mutableStateOf<NamePrompt?>(null) }

    fun offerNameOrLeave(appearance: Appearance, offeredName: String?) {
        val detectedName = source?.detectedName
        namePrompt = offeredName?.let { NamePrompt(appearance, it, clearsCustomName = it == detectedName) }
        if (offeredName == null) vm.back()
    }

    GridScreen(
        title = if (source == null) stringResource(R.string.icon_pack) else stringResource(R.string.icons_title_for, source.name),
        description = stringResource(R.string.icon_pack_description),
        preview = source?.let { { SourceAvatar(it.appearance, it.name, 72.dp) } },
    ) {
        if (source != null) {
            SectionHeader(stringResource(R.string.automatic))
            TileGrid {
                AvatarTile(
                    appearance = source.detectedAppearance,
                    name = source.name,
                    avatarSize = tileSize,
                    label = source.detectedName,
                    labelWidth = labelWidth,
                    labelStyle = labelStyle,
                    labelMaxLines = 2,
                    selected = selectedKey == KEY_AUTOMATIC,
                    onClick = {
                        vm.setIcon(source.id, null, null)
                        offerNameOrLeave(source.detectedAppearance, IconPreset.offerName(source.detectedName, source.name))
                    },
                    modifier = plan.item(KEY_AUTOMATIC),
                )
            }
        }
        presets.groupBy { it.category }.forEach { (category, items) ->
            SectionHeader(categoryName(category))
            TileGrid {
                items.forEach { preset ->
                    val appearance = Appearance(preset.glyph, preset.palette)
                    AvatarTile(
                        appearance = appearance,
                        name = source?.name ?: preset.name,
                        avatarSize = tileSize,
                        label = preset.name,
                        labelWidth = labelWidth,
                        labelStyle = labelStyle,
                        labelMaxLines = 2,
                        selected = preset.id == selectedKey,
                        onClick = {
                            if (source != null) {
                                vm.setIcon(source.id, preset.glyph.name, preset.palette.name)
                                offerNameOrLeave(appearance, preset.nameToOffer(source.name))
                            }
                        },
                        modifier = plan.item(preset.id),
                    )
                }
            }
        }
    }

    val prompt = namePrompt
    if (source != null && prompt != null) {
        NamePromptDialog(
            prompt = prompt,
            currentName = source.name,
            onUse = {
                vm.rename(source.id, if (prompt.clearsCustomName) null else prompt.name)
                namePrompt = null
                vm.back()
            },
            onKeep = {
                namePrompt = null
                vm.back()
            },
        )
    }
}

@Composable
private fun NamePromptDialog(prompt: NamePrompt, currentName: String, onUse: () -> Unit, onKeep: () -> Unit) {
    Dialog(onDismissRequest = onKeep, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val useFocus = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            withFrameNanos { }
            runCatching { useFocus.requestFocus() }
        }
        Column(
            modifier = Modifier
                .widthIn(min = 520.dp, max = 720.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(LauncherColors.Sheet)
                .padding(horizontal = 56.dp, vertical = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SourceAvatar(prompt.appearance, prompt.name, 96.dp)
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.name_prompt_title, prompt.name),
                style = MaterialTheme.typography.headlineSmall,
                color = LauncherColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.name_prompt_current, currentName),
                style = MaterialTheme.typography.bodyLarge,
                color = LauncherColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PillButton(stringResource(R.string.name_prompt_use), onUse, Modifier.focusRequester(useFocus))
                PillButton(stringResource(R.string.name_prompt_keep), onKeep)
            }
        }
    }
}

// endregion

// region Colour picker

@Composable
fun ColorPickerScreen(vm: LauncherViewModel, state: LauncherState, id: String) {
    val source = state.source(id) ?: return
    val hasCustomPalette = state.settings.override(id).palette != null
    val selectedKey = if (hasCustomPalette) source.appearance.palette.name else KEY_AUTOMATIC
    val keys = listOf(KEY_AUTOMATIC) + Palette.entries.map { it.name }
    val plan = rememberFocusPlan(Screen.ColorPicker(id).key, vm.focusMemory, keys, fallback = selectedKey)
    val tileSize = 84.dp

    GridScreen(
        title = stringResource(R.string.colors_title_for, source.name),
        description = stringResource(R.string.colors_description),
        preview = { SourceAvatar(source.appearance, source.name, 72.dp) },
    ) {
        Spacer(Modifier.height(16.dp))
        TileGrid {
            AvatarTile(
                appearance = Appearance(source.appearance.glyph, source.detectedAppearance.palette),
                name = source.name,
                avatarSize = tileSize,
                label = stringResource(R.string.automatic),
                selected = selectedKey == KEY_AUTOMATIC,
                onClick = {
                    vm.setPalette(id, null)
                    vm.back()
                },
                modifier = plan.item(KEY_AUTOMATIC),
            )
            Palette.entries.forEach { palette ->
                AvatarTile(
                    appearance = Appearance(source.appearance.glyph, palette),
                    name = source.name,
                    avatarSize = tileSize,
                    label = null,
                    selected = selectedKey == palette.name,
                    onClick = {
                        vm.setPalette(id, palette.name)
                        vm.back()
                    },
                    modifier = plan.item(palette.name),
                )
            }
        }
    }
}

// endregion

// region Reorder

@Composable
fun ReorderScreen(vm: LauncherViewModel, state: LauncherState) {
    val sources = state.all
    var moving by remember { mutableStateOf<String?>(null) }
    val ids = sources.map { it.id }
    val plan = rememberFocusPlan(Screen.Reorder.key, vm.focusMemory, ids)

    // Keep the focus on the tile being carried after the list is re-ordered.
    LaunchedEffect(ids) { moving?.let(plan::focus) }
    BackHandler(enabled = moving != null) { moving = null }

    val count = sources.size
    val avatarSize = when {
        count <= 4 -> 120.dp
        count <= 6 -> 96.dp
        else -> 80.dp
    }
    val hidden = stringResource(R.string.source_hidden)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.settings_reorder),
            style = MaterialTheme.typography.displaySmall,
            color = LauncherColors.TextPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_reorder_sub),
            style = MaterialTheme.typography.bodyLarge,
            color = LauncherColors.TextSecondary,
        )
        Spacer(Modifier.height(40.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .widthIn(min = maxWidth)
                    .padding(horizontal = 48.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(if (count <= 4) 32.dp else 16.dp, Alignment.CenterHorizontally),
            ) {
                sources.forEachIndexed { index, source ->
                    key(source.id) {
                        val isMoving = moving == source.id
                        val isOtherMoving = moving != null && !isMoving
                        val position = "${index + 1}"
                        AvatarTile(
                            appearance = source.appearance,
                            name = source.name,
                            avatarSize = avatarSize,
                            sublabel = when {
                                isMoving -> "◀  $position  ▶"
                                source.hidden -> "$position · $hidden"
                                else -> position
                            },
                            dimmed = source.hidden || isOtherMoving,
                            lifted = isMoving,
                            onClick = { moving = if (isMoving) null else source.id },
                            modifier = plan.item(source.id).onPreviewKeyEvent { event ->
                                val isKeyDown = event.type == KeyEventType.KeyDown
                                if (!isMoving || !isKeyDown) return@onPreviewKeyEvent false
                                when (event.key) {
                                    Key.DirectionLeft -> { vm.move(source.id, -1); true }
                                    Key.DirectionRight -> { vm.move(source.id, 1); true }
                                    Key.DirectionUp, Key.DirectionDown -> true
                                    else -> false
                                }
                            },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(if (moving == null) R.string.reorder_hint_idle else R.string.reorder_hint_moving),
            style = MaterialTheme.typography.bodyLarge,
            color = LauncherColors.TextTertiary,
            textAlign = TextAlign.Center,
        )
    }
}

// endregion
