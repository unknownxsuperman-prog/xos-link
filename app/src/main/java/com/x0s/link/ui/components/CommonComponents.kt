package com.x0s.link.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.x0s.link.ui.theme.BadgeBlue
import com.x0s.link.ui.theme.BadgeGold
import com.x0s.link.ui.theme.BadgeGreen

/** Renders the gold/blue/green verification badge used across posts, profiles and chips. */
@Composable
fun VerifiedBadge(type: String?, size: androidx.compose.ui.unit.Dp = 14.dp, modifier: Modifier = Modifier) {
    val color = when (type) {
        "gold" -> BadgeGold
        "blue" -> BadgeBlue
        "green" -> BadgeGreen
        else -> null
    } ?: return
    Icon(
        imageVector = if (type == "gold") Icons.Filled.Verified else Icons.Filled.CheckCircle,
        contentDescription = "Verified ($type)",
        tint = color,
        modifier = modifier.size(size)
    )
}

/** Square avatar (10dp corner radius, matches --avatars: square; border-radius:10px in the CSS). */
@Composable
fun SquareAvatar(url: String?, sizeDp: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
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
fun CircleAvatar(url: String?, sizeDp: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
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
fun NameWithBadge(name: String, verified: String?, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
        if (verified != null && verified != "none") {
            androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
            VerifiedBadge(verified)
        }
    }
}
