package dev.germar.sourcelauncher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.germar.sourcelauncher.model.Appearance
import dev.germar.sourcelauncher.ui.icons.SourceAvatar
import dev.germar.sourcelauncher.ui.theme.LauncherColors

// region Focus

/**
 * Hands out a [FocusRequester] per item key, remembers the last focused item of a screen and puts
 * the initial focus on it (or on [fallback]) when the screen appears, its items change or [trigger] changes.
 */
class FocusPlan internal constructor(private val onFocused: (String) -> Unit) {
    private val requesters = mutableMapOf<String, FocusRequester>()

    /**
     * False until the initial focus has been placed. Compose moves focus on its own before that (e.g. to
     * the first focusable when the window gains focus), and that must not overwrite the remembered item.
     */
    private var settled = false

    fun item(key: String): Modifier = Modifier
        .focusRequester(requesters.getOrPut(key) { FocusRequester() })
        .onFocusChanged { state ->
            val isUserFocus = state.isFocused && settled
            if (isUserFocus) onFocused(key)
        }

    fun focus(key: String?): Boolean {
        val requester = key?.let { requesters[it] } ?: return false
        return runCatching { requester.requestFocus() }.isSuccess
    }

    internal fun settleOn(key: String) {
        focus(key)
        onFocused(key)
        settled = true
    }
}

@Composable
fun rememberFocusPlan(
    memoryKey: String,
    memory: MutableMap<String, String>,
    keys: List<String>,
    fallback: String? = null,
    trigger: Any? = Unit,
): FocusPlan {
    // A fresh plan whenever the items change starts unsettled until the initial focus is placed.
    val plan = remember(memoryKey, keys, trigger) { FocusPlan { memory[memoryKey] = it } }
    val currentFallback by rememberUpdatedState(fallback)
    LaunchedEffect(plan) {
        if (keys.isEmpty()) return@LaunchedEffect
        withFrameNanos { }
        val remembered = memory[memoryKey]?.takeIf { it in keys }
        val preferred = currentFallback?.takeIf { it in keys }
        plan.settleOn(remembered ?: preferred ?: keys.first())
    }
    return plan
}

// endregion

// region Layout

val ScreenBackground: Brush = Brush.verticalGradient(listOf(LauncherColors.BackgroundTop, LauncherColors.BackgroundBottom))

/** tvOS-style settings layout: a hero and title on the left, a scrolling list on the right. */
@Composable
fun TwoPaneScreen(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    hero: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(modifier = modifier.fillMaxSize().padding(horizontal = 56.dp)) {
        Column(
            modifier = Modifier.weight(0.42f).fillMaxHeight().padding(vertical = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (hero != null) {
                hero()
                Spacer(Modifier.size(28.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = LauncherColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            if (description != null) {
                Spacer(Modifier.size(10.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = LauncherColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.width(48.dp))
        Column(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 56.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content,
        )
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = LauncherColors.TextTertiary,
        modifier = modifier.padding(start = 20.dp, top = 20.dp, bottom = 6.dp),
    )
}

// endregion

// region Rows

@Composable
fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    showChevron: Boolean = false,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable (focused: Boolean) -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        interactionSource = interactionSource,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(14.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = LauncherColors.TextPrimary,
            focusedContainerColor = LauncherColors.RowFocused,
            focusedContentColor = LauncherColors.RowFocusedText,
            pressedContainerColor = LauncherColors.RowFocused.copy(alpha = 0.85f),
            pressedContentColor = LauncherColors.RowFocusedText,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = LauncherColors.TextTertiary,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalContentColor.current.copy(alpha = 0.62f),
                    )
                }
            }
            if (value != null) {
                Spacer(Modifier.width(16.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalContentColor.current.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (trailing != null) {
                Spacer(Modifier.width(16.dp))
                trailing(focused)
            }
            if (showChevron) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = LocalContentColor.current.copy(alpha = 0.45f),
                )
            }
        }
    }
}

@Composable
fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    SettingsRow(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        trailing = { focused -> TogglePill(checked = checked, onLightBackground = focused) },
    )
}

@Composable
fun CheckRow(
    title: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    SettingsRow(
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        modifier = modifier,
        leading = leading,
        trailing = {
            if (checked) Icon(Icons.Filled.Check, contentDescription = null, tint = LocalContentColor.current)
        },
    )
}

private val ToggleOn = Color(0xFF34C759)

@Composable
fun TogglePill(checked: Boolean, onLightBackground: Boolean) {
    val trackOff = if (onLightBackground) Color(0x33000000) else Color(0x33FFFFFF)
    val track by animateColorAsState(if (checked) ToggleOn else trackOff, label = "toggleTrack")
    val knobOffset by animateDpAsState(if (checked) 20.dp else 0.dp, label = "toggleKnob")
    Box(
        modifier = Modifier
            .size(width = 50.dp, height = 30.dp)
            .clip(CircleShape)
            .background(track)
            .padding(3.dp),
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffset.roundToPx(), 0) }
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

/** A rounded text button for empty states and dialogs. */
@Composable
fun PillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = LauncherColors.TextPrimary,
            focusedContainerColor = LauncherColors.RowFocused,
            focusedContentColor = LauncherColors.RowFocusedText,
            pressedContainerColor = LauncherColors.RowFocused,
            pressedContentColor = LauncherColors.RowFocusedText,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
        )
    }
}

// endregion

// region Avatar tiles

/**
 * The round, focusable avatar used on the home screen and in pickers. Draws a white focus ring, or
 * a countdown ring when [progress] (1 → 0) is set.
 */
@Composable
fun AvatarTile(
    appearance: Appearance,
    name: String,
    avatarSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = name,
    sublabel: String? = null,
    dimmed: Boolean = false,
    selected: Boolean = false,
    lifted: Boolean = false,
    progress: Float? = null,
    labelWidth: Dp = avatarSize + 44.dp,
    labelStyle: TextStyle = MaterialTheme.typography.titleMedium,
    labelMaxLines: Int = 1,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val ringGap = (avatarSize.value * 0.075f).coerceAtLeast(6f).dp
    val surfaceSize = avatarSize + ringGap * 2
    val lift by animateDpAsState(if (lifted) (-14).dp else 0.dp, label = "lift")
    val showDimmed = dimmed && !focused
    val labelAlpha = if (showDimmed) 0.5f else 1f

    Column(
        modifier = Modifier.width(labelWidth).graphicsLayer { translationY = lift.toPx() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Surface(
                onClick = onClick,
                modifier = modifier.size(surfaceSize),
                interactionSource = interactionSource,
                shape = ClickableSurfaceDefaults.shape(CircleShape),
                scale = ClickableSurfaceDefaults.scale(focusedScale = if (lifted) 1.16f else 1.1f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    pressedContainerColor = Color.Transparent,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val ringWidth = if (progress != null) 4.dp.toPx() else 3.dp.toPx()
                            val inset = ringWidth / 2f
                            val arcSize = androidx.compose.ui.geometry.Size(size.width - ringWidth, size.height - ringWidth)
                            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
                            if (progress != null) {
                                drawCircle(Color.White.copy(alpha = 0.22f), radius = size.minDimension / 2f - inset, style = Stroke(ringWidth))
                                drawArc(
                                    color = Color.White,
                                    startAngle = -90f,
                                    sweepAngle = 360f * progress,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(ringWidth, cap = StrokeCap.Round),
                                )
                            } else if (focused) {
                                drawCircle(Color.White, radius = size.minDimension / 2f - inset, style = Stroke(ringWidth))
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    SourceAvatar(appearance = appearance, name = name, size = avatarSize, dimmed = showDimmed)
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .zIndex(2f)
                        .size((avatarSize.value * 0.26f).coerceAtLeast(22f).dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = LauncherColors.RowFocusedText,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                    )
                }
            }
        }
        if (label != null) {
            Spacer(Modifier.size(14.dp))
            Text(
                text = label,
                style = labelStyle,
                color = (if (focused) LauncherColors.TextPrimary else LauncherColors.TextSecondary).let { it.copy(alpha = it.alpha * labelAlpha) },
                maxLines = labelMaxLines,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        if (sublabel != null) {
            Spacer(Modifier.size(2.dp))
            Text(
                text = sublabel,
                style = MaterialTheme.typography.bodySmall,
                color = LauncherColors.TextTertiary.copy(alpha = LauncherColors.TextTertiary.alpha * labelAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** A small avatar for list rows. */
@Composable
fun RowAvatar(appearance: Appearance, name: String, dimmed: Boolean = false) {
    SourceAvatar(appearance = appearance, name = name, size = 36.dp, dimmed = dimmed)
}

/** A thin rounded border used to frame text inputs. */
val InputBorder = BorderStroke(2.dp, Color.White)

// endregion
