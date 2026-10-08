package com.bettyai.assistant.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bettyai.assistant.ui.theme.BettyCyan
import com.bettyai.assistant.ui.theme.BettyViolet

/**
 * Betty's logo: a set of voice-equalizer bars inside a soft gradient glow.
 * When [animated] is true the bars gently pulse, giving the assistant a "living"
 * presence on the home screen.
 */
@Composable
fun BettyLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    animated: Boolean = true,
    barColor: Color = Color.White
) {
    val transition = rememberInfiniteTransition(label = "betty_logo")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val radius = w / 2f

            // Outer glow ring.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BettyViolet.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(w / 2f, h / 2f),
                    radius = radius
                ),
                radius = radius,
                center = Offset(w / 2f, h / 2f)
            )

            // Inner gradient disc.
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(BettyViolet, BettyCyan),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                ),
                radius = radius * 0.72f,
                center = Offset(w / 2f, h / 2f)
            )

            // Equalizer bars.
            val barCount = 5
            val barWidth = w * 0.075f
            val gap = w * 0.055f
            val totalWidth = barCount * barWidth + (barCount - 1) * gap
            val startX = (w - totalWidth) / 2f
            val centerY = h / 2f
            val maxBarHeight = h * 0.42f

            val baseHeights = listOf(0.45f, 0.75f, 1.0f, 0.75f, 0.45f)
            baseHeights.forEachIndexed { index, base ->
                val animatedFactor = if (animated) {
                    0.7f + 0.3f * ((phase + index * 0.18f) % 1f)
                } else 1f
                val barHeight = maxBarHeight * base * animatedFactor
                val x = startX + index * (barWidth + gap)
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, centerY - barHeight / 2f),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
