package com.x0s.link.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.x0s.link.data.model.XosCollege
import com.x0s.link.data.model.XosPost
import com.x0s.link.data.model.XosProfile
import com.x0s.link.ui.components.AccentPill
import com.x0s.link.ui.components.NameWithBadge
import com.x0s.link.ui.theme.XosLinkTheme

/**
 * Fake data for previews only - never used at runtime. Lets you see the XOS design
 * language (AMOLED background, blue accent, verified badges, pills) directly in Android
 * Studio's Split/Design view without a device, emulator, network, or ViewModel wiring.
 *
 * Open this file, then click "Split" or "Design" in the top-right of the editor.
 */
private val previewProfile = XosProfile(
    userid = "nikhil",
    handle = "anush@x0s",
    displayName = "Anush Decodes",
    verified = "gold",
    avatar = "",
    bio = "Building x0s. Zero expectations, high confidence.",
    followers = listOf("a", "b", "c"),
    following = listOf("a", "b"),
    posts = listOf(
        XosPost(type = "image", files = listOf(""), caption = "Deep logic toggle.", likes = 42, time = "2h ago")
    )
)

private val previewCollege = XosCollege(
    id = "rce",
    name = "RV College of Engineering, Bangalore",
    pfp = "",
    pill1 = "Engineering",
    pill2 = "Bangalore"
)

@Preview(name = "Dark - Profile header", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ProfileHeaderPreview() {
    XosLinkTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(20.dp)) {
                NameWithBadge(previewProfile.displayName, previewProfile.verified)
                Spacer(Modifier.height(4.dp))
                Text(previewProfile.handle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text(previewProfile.bio, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(name = "Dark - College pills", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CollegePillsPreview() {
    XosLinkTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(Modifier.padding(20.dp)) {
                AccentPill(previewCollege.pill1)
                Spacer(Modifier.width(8.dp))
                AccentPill(previewCollege.pill2)
            }
        }
    }
}

@Preview(name = "Dark - Buttons", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ButtonsPreview() {
    XosLinkTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {}) { Text("Follow") }
                OutlinedButton(onClick = {}) { Text("Following") }
            }
        }
    }
}

@Preview(name = "Light - Profile header", showBackground = true, backgroundColor = 0xFFF2F2F7)
@Composable
private fun ProfileHeaderLightPreview() {
    XosLinkTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(20.dp)) {
                NameWithBadge(previewProfile.displayName, previewProfile.verified)
                Spacer(Modifier.height(4.dp))
                Text(previewProfile.handle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
