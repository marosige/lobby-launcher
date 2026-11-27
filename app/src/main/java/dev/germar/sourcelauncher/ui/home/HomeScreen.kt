package dev.germar.sourcelauncher.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.germar.sourcelauncher.R
import dev.germar.sourcelauncher.model.Appearance
import dev.germar.sourcelauncher.model.AutoSelectPolicy
import dev.germar.sourcelauncher.model.ClockFormat
import dev.germar.sourcelauncher.model.ConnectionState
import dev.germar.sourcelauncher.model.Glyph
import dev.germar.sourcelauncher.model.Palette
import dev.germar.sourcelauncher.model.ResolvedSource
import dev.germar.sourcelauncher.ui.Countdown
import dev.germar.sourcelauncher.ui.LauncherState
import dev.germar.sourcelauncher.ui.SystemStatus
import dev.germar.sourcelauncher.ui.components.AvatarTile
import dev.germar.sourcelauncher.ui.components.FocusPlan
import dev.germar.sourcelauncher.ui.components.PillButton
import dev.germar.sourcelauncher.ui.components.clockFormatter
import dev.germar.sourcelauncher.ui.components.rememberFocusPlan
import dev.germar.sourcelauncher.ui.icons.SourceAvatar
import dev.germar.sourcelauncher.ui.icons.topColor
import dev.germar.sourcelauncher.ui.theme.LauncherColors
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.math.ceil

private const val KEY_TV_HOME = "action:tv_home"
private const val KEY_TV_SETTINGS = "action:tv_settings"
private const val KEY_CUSTOMISE = "action:customise"
private const val KEY_DEMO = "action:demo"

@Composable
fun HomeScreen(
    state: LauncherState,
    countdown: Countdown?,
    system: SystemStatus,
    homeVisit: Long,
    focusMemory: MutableMap<String, String>,
    onSwitch: (ResolvedSource) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTvSettings: () -> Unit,
    onOpenStockHome: () -> Unit,
    onEnableDemo: () -> Unit,
) {
    val sources = state.home
    val isEmpty = state.loaded && sources.isEmpty()
    val actionKeys = buildList {
        if (isEmpty) add(KEY_DEMO)
        if (system.hasStockHome) add(KEY_TV_HOME)
        add(KEY_TV_SETTINGS)
        add(KEY_CUSTOMISE)
    }
    val keys = if (state.loaded) sources.map { it.id } + actionKeys else emptyList()
    val preferred = countdown?.targetId ?: AutoSelectPolicy.initialFocus(sources, state.settings)?.id ?: actionKeys.first()
    val plan = rememberFocusPlan(
        memoryKey = "home",
        memory = focusMemory,
        keys = keys,
        fallback = preferred,
        trigger = homeVisit,
    )

    var focusedId by remember { mutableStateOf<String?>(null) }
    val focusedPalette = sources.firstOrNull { it.id == focusedId }?.appearance?.palette
    val glow by animateColorAsState(
        targetValue = focusedPalette?.topColor?.copy(alpha = 0.16f) ?: Color.Transparent,
        animationSpec = tween(700),
        label = "ambientGlow",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(glow, Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.45f),
                        radius = size.width * 0.6f,
                    ),
                )
            },
    ) {
        Clock(state.settings.clockFormat, Modifier.align(Alignment.TopEnd).padding(top = 32.dp, end = 52.dp))

        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                when {
                    !state.loaded -> Unit
                    isEmpty -> EmptyState(plan = plan, onEnableDemo = onEnableDemo)
                    else -> SourcePicker(
                        sources = sources,
                        countdown = countdown,
                        plan = plan,
                        onFocused = { focusedId = it },
                        onSwitch = onSwitch,
                    )
                }
            }
            SmallActions(
                plan = plan,
                showTvHome = system.hasStockHome,
                onOpenStockHome = onOpenStockHome,
                onOpenTvSettings = onOpenTvSettings,
                onOpenSettings = onOpenSettings,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SourcePicker(
    sources: List<ResolvedSource>,
    countdown: Countdown?,
    plan: FocusPlan,
    onFocused: (String) -> Unit,
    onSwitch: (ResolvedSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(1f) }
    LaunchedEffect(countdown?.token) {
        if (countdown == null) return@LaunchedEffect
        plan.focus(countdown.targetId)
        progress.snapTo(1f)
        progress.animateTo(0f, tween(countdown.durationMillis.toInt(), easing = LinearEasing))
    }
    val countdownTarget = sources.firstOrNull { it.id == countdown?.targetId }

    val count = sources.size
    val avatarSize = when {
        count <= 4 -> 140.dp
        count <= 6 -> 100.dp
        else -> 88.dp
    }
    val gap = if (count <= 4) 40.dp else 18.dp

    // The countdown sits where a heading would be; the same space is reserved below the row so the
    // avatars stay vertically centred whether or not a countdown is running.
    val countdownAreaHeight = 64.dp
    val countdownGap = 28.dp
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.height(countdownAreaHeight), contentAlignment = Alignment.BottomCenter) {
            if (countdownTarget != null) {
                val totalSeconds = countdown!!.durationMillis / 1000f
                val secondsLeft = ceil(progress.value * totalSeconds).toInt().coerceAtLeast(1)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.home_countdown, countdownTarget.name, secondsLeft),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LauncherColors.TextPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_countdown_cancel),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LauncherColors.TextTertiary,
                    )
                }
            }
        }
        Spacer(Modifier.height(countdownGap))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .widthIn(min = maxWidth)
                    .padding(horizontal = 48.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            ) {
                sources.forEach { source ->
                    key(source.id) {
                        val isCountdownTarget = countdownTarget?.id == source.id
                        AvatarTile(
                            appearance = source.appearance,
                            name = source.name,
                            avatarSize = avatarSize,
                            sublabel = sublabelFor(source),
                            dimmed = source.isDisconnected,
                            progress = if (isCountdownTarget) progress.value else null,
                            onClick = { onSwitch(source) },
                            modifier = plan.item(source.id).onFocusChanged { if (it.isFocused) onFocused(source.id) },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(countdownAreaHeight + countdownGap))
    }
}

@Composable
private fun sublabelFor(source: ResolvedSource): String? = when (source.connection) {
    ConnectionState.DISCONNECTED -> stringResource(R.string.status_disconnected)
    ConnectionState.STANDBY -> listOfNotNull(source.portLabel, stringResource(R.string.status_standby)).joinToString(" · ")
    else -> source.portLabel
}

@Composable
private fun EmptyState(plan: FocusPlan, onEnableDemo: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.widthIn(max = 560.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SourceAvatar(appearance = Appearance(Glyph.HDMI, Palette.SILVER), name = "", size = 96.dp)
        Spacer(Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.empty_title),
            style = MaterialTheme.typography.headlineMedium,
            color = LauncherColors.TextPrimary,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.empty_body),
            style = MaterialTheme.typography.bodyLarge,
            color = LauncherColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        PillButton(
            text = stringResource(R.string.empty_demo),
            onClick = onEnableDemo,
            modifier = plan.item(KEY_DEMO),
        )
    }
}

@Composable
private fun SmallActions(
    plan: FocusPlan,
    showTvHome: Boolean,
    onOpenStockHome: () -> Unit,
    onOpenTvSettings: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        if (showTvHome) {
            SmallAction(Icons.Filled.Home, stringResource(R.string.action_tv_home), onOpenStockHome, plan.item(KEY_TV_HOME))
        }
        SmallAction(Icons.Filled.Settings, stringResource(R.string.action_tv_settings), onOpenTvSettings, plan.item(KEY_TV_SETTINGS))
        SmallAction(Icons.Filled.Edit, stringResource(R.string.action_customise), onOpenSettings, plan.item(KEY_CUSTOMISE))
    }
}

@Composable
private fun SmallAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = modifier.size(40.dp),
            interactionSource = interactionSource,
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.15f),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.08f),
                contentColor = LauncherColors.TextSecondary,
                focusedContainerColor = LauncherColors.RowFocused,
                focusedContentColor = LauncherColors.RowFocusedText,
                pressedContainerColor = LauncherColors.RowFocused,
                pressedContentColor = LauncherColors.RowFocusedText,
            ),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (focused) LauncherColors.TextPrimary else LauncherColors.TextTertiary,
        )
    }
}

@Composable
private fun Clock(clockFormat: ClockFormat, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val format = remember(context, clockFormat) { clockFormatter(context, clockFormat) }
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }
    Text(
        text = format.format(now),
        modifier = modifier,
        fontSize = 26.sp,
        fontWeight = FontWeight.Light,
        color = LauncherColors.TextSecondary,
    )
}
