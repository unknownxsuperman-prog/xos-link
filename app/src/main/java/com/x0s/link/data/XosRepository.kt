package com.x0s.link.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object XosRepository {

    // Matches the base URL pattern the user pointed us at.
    const val DATA_BASE = "https://unknownxsuperman-prog.github.io/anushdecodes/"

    private suspend fun fetchText(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            if (conn.responseCode !in 200..299) return@withContext null
            conn.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loadProfiles(): Map<String, XosProfile> {
        val js = fetchText(DATA_BASE + "profiles.js")
        if (js != null) {
            val literal = JsObjectExtractor.extractLiteral(js, "XOS_PROFILES")
            if (literal != null) {
                try {
                    val json = JSONObject(JsObjectExtractor.toJson(literal))
                    val out = LinkedHashMap<String, XosProfile>()
                    json.keys().forEach { key ->
                        val o = json.getJSONObject(key)
                        out[key] = parseProfile(o, key)
                    }
                    if (out.isNotEmpty()) return out
                } catch (e: Exception) { /* fall through to fallback */ }
            }
        }
        return fallbackProfiles()
    }

    suspend fun loadColleges(): List<XosCollege> {
        val js = fetchText(DATA_BASE + "colleges.js")
        if (js != null) {
            val literal = JsObjectExtractor.extractLiteral(js, "XOS_COLLEGES")
            if (literal != null) {
                try {
                    val arr = JSONArray(JsObjectExtractor.toJson(literal))
                    val out = ArrayList<XosCollege>()
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        out.add(
                            XosCollege(
                                id = o.optString("id", ""),
                                name = o.optString("name", ""),
                                pfp = o.optString("pfp", ""),
                                pill1 = o.optString("pill1", ""),
                                pill2 = o.optString("pill2", "")
                            )
                        )
                    }
                    if (out.isNotEmpty()) return out
                } catch (e: Exception) { /* fall through */ }
            }
        }
        return fallbackColleges()
    }

    private fun parseProfile(o: JSONObject, key: String): XosProfile {
        val postsArr = o.optJSONArray("posts")
        val posts = ArrayList<XosPost>()
        if (postsArr != null) {
            for (i in 0 until postsArr.length()) {
                val p = postsArr.getJSONObject(i)
                val filesArr = p.optJSONArray("files")
                val files = ArrayList<String>()
                if (filesArr != null) for (j in 0 until filesArr.length()) files.add(filesArr.getString(j))
                posts.add(
                    XosPost(
                        type = p.optString("type", "image"),
                        files = files,
                        caption = p.optString("caption", ""),
                        likes = p.optInt("likes", 0),
                        time = p.optString("time", "")
                    )
                )
            }
        }
        fun strList(name: String): List<String> {
            val a = o.optJSONArray(name) ?: return emptyList()
            return (0 until a.length()).map { a.getString(it) }
        }
        return XosProfile(
            userid = o.optString("userid", key),
            handle = o.optString("handle", "@$key"),
            displayName = o.optString("displayName", key),
            verified = badgeFromKey(o.optString("verified", "none")),
            avatar = resolveAsset(o.optString("avatar", "")),
            following = strList("following"),
            followers = strList("followers"),
            posts = posts.map { it.copy(files = it.files.map { f -> resolveAsset(f) }) }
        )
    }

    /** profiles.js references bare filenames (e.g. "avatar.png") hosted alongside it. */
    private fun resolveAsset(name: String): String {
        if (name.isBlank()) return ""
        if (name.startsWith("http")) return name
        return DATA_BASE + name
    }

    // ── Fallback data — mirrors the inline <script> defaults in index.html exactly ──
    fun fallbackProfiles(): Map<String, XosProfile> = linkedMapOf(
        "nikhil" to XosProfile(
            userid = "nikhil",
            handle = "anush@x0s",
            displayName = "Anush Decodes",
            verified = Badge.GOLD,
            avatar = DATA_BASE + "Screenshot_20260418-234611.png",
            following = listOf("gemini"),
            followers = listOf("gemini"),
            posts = listOf(
                XosPost("image", listOf(DATA_BASE + "file_00000000862871fab1161995ce9f104e.png"), "Deep logic toggle.", 42, "2h ago"),
                XosPost("video", listOf(DATA_BASE + "lv_7587788892909276421_20260417222425.mp4"), "\u2728\u2728", 128, "1d ago"),
                XosPost("video", listOf(DATA_BASE + "Video-784.mp4"), "\uD83D\uDE0A\uD83D\uDE0A", 42, "2h ago")
            )
        ),
        "gemini" to XosProfile(
            userid = "gemini",
            handle = "gemini@x0s",
            displayName = "Gemini X",
            verified = Badge.BLUE,
            avatar = DATA_BASE + "avatar_gemini.png",
            following = listOf("nikhil"),
            followers = listOf("nikhil"),
            posts = listOf(
                XosPost("image", listOf(DATA_BASE + "gemini_post1.jpg"), "First light.", 213, "3h ago"),
                XosPost("video", listOf(DATA_BASE + "gemini_video1.mp4"), "Neural walk", 401, "1d ago")
            )
        ),
        "niranjan" to XosProfile(
            userid = "niranjan",
            handle = "niranjan@xos",
            displayName = "Niranjan",
            verified = Badge.GREEN,
            avatar = DATA_BASE + "1770636198936.png",
            following = listOf("gemini"),
            followers = listOf("gemini"),
            posts = listOf(
                XosPost("video", listOf(DATA_BASE + "nirup1.mp4"), "\uD83D\uDD49\uFE0F", 128, "1d ago")
            )
        )
    )

    fun fallbackColleges(): List<XosCollege> = listOf(
        XosCollege("E001", "RV College of Engineering, Bangalore", DATA_BASE + "1000046129.png", "Engineering", "Bangalore"),
        XosCollege("E002", "BMS College of Engineering, Bangalore", DATA_BASE + "1000046129.png", "Engineering", "Bangalore"),
        XosCollege("E003", "PES University, Bangalore", DATA_BASE + "1000046129.png", "University", "Bangalore")
    )

    // ── Messages mock data — mirrors MSGS_DATA in index.html ──
    fun mockConversations(): List<MsgConversation> = listOf(
        MsgConversation(1, "Alex", "alex@x0s", "https://api.dicebear.com/8.x/notionists/svg?seed=Alex&backgroundColor=111111", "See you tomorrow.", "11:24 AM", true, true, mutableListOf(
            ChatMsg("Hey! you free tomorrow?", false, "11:20 AM"),
            ChatMsg("Yeah what's up", true, "11:21 AM"),
            ChatMsg("Let's meet at campus", false, "11:22 AM"),
            ChatMsg("Sure works for me", true, "11:23 AM"),
            ChatMsg("See you tomorrow.", false, "11:24 AM")
        )),
        MsgConversation(2, "Nova", "nova@x0s", "https://api.dicebear.com/8.x/notionists/svg?seed=Nova&backgroundColor=0a0a0a", "Sent you a photo", "10:08 AM", true, false, mutableListOf(
            ChatMsg("Check this out", false, "10:05 AM"),
            ChatMsg("Photo", false, "10:08 AM", isImage = true)
        )),
        MsgConversation(3, "Jay", "jay@x0s", "https://api.dicebear.com/8.x/notionists/svg?seed=Jay&backgroundColor=0d0d0d", "That was insane!", "9:47 AM", false, true, mutableListOf(
            ChatMsg("Did you see the match?", false, "9:45 AM"),
            ChatMsg("Yes bro! unreal finish", true, "9:46 AM"),
            ChatMsg("That was insane!", false, "9:47 AM")
        )),
        MsgConversation(4, "Lucas", "lucas@x0s", "https://api.dicebear.com/8.x/notionists/svg?seed=Lucas&backgroundColor=080808", "Let's catch up soon.", "8:31 AM", true, false, mutableListOf(
            ChatMsg("Haven't seen you in a while", false, "8:28 AM"),
            ChatMsg("I know right, been busy", true, "8:30 AM"),
            ChatMsg("Let's catch up soon.", false, "8:31 AM")
        ))
    )
}
