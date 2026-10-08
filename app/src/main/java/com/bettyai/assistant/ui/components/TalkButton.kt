package com.bettyai.assistant.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bettyai.assistant.core.state.BettyState
import com.bettyai.assistant.ui.theme.BettyCyan
import com.bettyai.assistant.ui.theme.BettyViolet

/**
 * The primary interaction: a large, central push-to-talk button. It reflects the
 * state machine — idle shows a mic, busy shows a stop icon, and a pulsing halo
 * communicates that Betty is listening/thinking/speaking.
 */
@Composable
fun TalkButton(
    state: BettyState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = 168.dp
) {
    val busy = state.isBusy
    val visual = state.visual()

    val targetColor = when {
        !enabled -> Color(0xFF3A3363)
        busy -> visual.color
        else -> BettyViolet
    }
    val buttonColor by animateColorAsState(targetValue = targetColor, label = "talk_color")

    val transition = rememberInfiniteTransition(label = "talk_halo")
    val haloScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (busy) 1.28f else 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (busy) 800 else 2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )
    val haloAlpha by transition.animateFloat(
        initialValue = if (busy) 0.45f else 0.22f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (busy) 800 else 2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.size(diameter),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing halo.
        Canvas(modifier = Modifier.size(diameter).scale(haloScale)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(buttonColor.copy(alpha = haloAlpha), Color.Transparent),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.minDimension / 2f
                ),
                radius = size.minDimension / 2f
            )
        }

        // Main button.
        Box(
            modifier = Modifier
                .size(diameter * 0.78f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(buttonColor, BettyCyan.copy(alpha = 0.9f))
                    )
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (busy) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (busy) "توقف" else "شروع گفتگو",
                tint = Color.White,
                modifier = Modifier.size(diameter * 0.3f)
            )
        }
    }
}
