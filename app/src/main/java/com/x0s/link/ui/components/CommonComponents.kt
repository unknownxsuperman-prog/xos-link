package com.x0s.link.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.x0s.link.ui.theme.BadgeBlue
import com.x0s.link.ui.theme.BadgeGold
import com.x0s.link.ui.theme.BadgeGreen
import kotlin.math.cos
import kotlin.math.sin

/**
 * Recreates the web app's badge-gold.png / badge-blue.svg / badge-green.svg: a scalloped
 * "seal" shape (12-point) filled with the badge color, with a white checkmark, plus the same
 * soft pulsing glow used by .post-verified.v-gold/.v-blue/.v-green (badgePulseGold/Blue/Green
 * keyframes, 2.5-2.8s ease-in-out infinite).
 */
@Composable
fun VerifiedBadge(type: String?, size: Dp = 14.dp, modifier: Modifier = Modifier) {
    val color = when (type) {
        "gold" -> BadgeGold
        "blue" -> BadgeBlue
        "green" -> BadgeGreen
        else -> null
    } ?: return

    val period = when (type) { "gold" -> 2500; else -> 2800 }
    val transition = rememberInfiniteTransition(label = "badgePulse")
    val glow by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(period, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(modifier = modifier.size(size * 1.9f), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.size(size * 1.9f)) {
            // soft glow halo behind the badge, standing in for the CSS drop-shadow pulse
            drawCircle(color = color.copy(alpha = glow * 0.5f), radius = this.size.minDimension / 2f)
        }
        androidx.compose.foundation.Canvas(Modifier.size(size)) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val outerR = this.size.minDimension / 2f
            val innerR = outerR * 0.82f
            val points = 12
            val path = Path()
            for (i in 0 until points * 2) {
                val angle = (Math.PI * i / points) - Math.PI / 2
                val r = if (i % 2 == 0) outerR else innerR
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = color)

            // checkmark
            val checkPath = Path().apply {
                moveTo(cx - outerR * 0.42f, cy + outerR * 0.02f)
                lineTo(cx - outerR * 0.08f, cy + outerR * 0.36f)
                lineTo(cx + outerR * 0.46f, cy - outerR * 0.32f)
            }
            drawPath(
                checkPath,
                color = Color.White,
                style = Stroke(width = outerR * 0.22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

/** Square avatar (10dp corner radius, matches --avatars: square; border-radius:10px in the CSS). */
@Composable
fun SquareAvatar(url: String?, sizeDp: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(sizeDp.value * 0.26f))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(sizeDp.value * 0.26f))
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(sizeDp)
        )
    }
}

@Composable
fun CircleAvatar(url: String?, sizeDp: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(sizeDp)
        )
    }
}

/** Small rounded accent pill, e.g. "Engineering" / "Bangalore" tags on college cards. */
@Composable
fun AccentPill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), RoundedCornerShape(99.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun NameWithBadge(name: String, verified: String?, modifier: Modifier = Modifier, maxLines: Int = 1) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(name, style = MaterialTheme.typography.titleSmall, maxLines = maxLines)
        if (verified != null && verified != "none") {
            androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
            VerifiedBadge(verified)
        }
    }
}

/**
 * Single-line text that scroll-marquees when it overflows its available width - mirrors the
 * web app's .college-link-name.marquee-anim behavior (only animates when the text doesn't fit).
 */
@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        fontSize = fontSize,
        color = color,
        maxLines = 1,
        modifier = modifier.basicMarquee(iterations = Int.MAX_VALUE)
    )
}
