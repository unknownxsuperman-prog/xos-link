package com.x0s.link.ui.messages

import androidx.lifecycle.ViewModel
import com.x0s.link.data.model.XosChatMessage
import com.x0s.link.data.model.XosConversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MessagesViewModel : ViewModel() {

    private val _conversations = MutableStateFlow(demoConversations())
    val conversations = _conversations.asStateFlow()

    fun search(query: String): List<XosConversation> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return _conversations.value
        return _conversations.value.filter {
            it.name.lowercase().contains(q) || it.preview.lowercase().contains(q)
        }
    }

    fun conversation(id: Int): XosConversation? = _conversations.value.find { it.id == id }

    fun markRead(id: Int) {
        _conversations.value = _conversations.value.map {
            if (it.id == id) it.copy(unread = false) else it
        }
    }

    fun sendMessage(id: Int, text: String) {
        _conversations.value = _conversations.value.map { conv ->
            if (conv.id == id) {
                val updatedMessages = (conv.messages + XosChatMessage(text, out = true, time = "now")).toMutableList()
                conv.copy(preview = text, messages = updatedMessages)
            } else conv
        }
    }
}

private fun demoConversations(): List<XosConversation> = listOf(
    XosConversation(
        1, "Alex", "alex@x0s",
        "https://api.dicebear.com/8.x/notionists/svg?seed=Alex&backgroundColor=111111",
        "See you tomorrow.", "11:24 AM", unread = true, online = true,
        mutableListOf(
            XosChatMessage("Hey! you free tomorrow?", false, "11:20 AM"),
            XosChatMessage("Yeah what's up", true, "11:21 AM"),
            XosChatMessage("Let's meet at campus", false, "11:22 AM"),
            XosChatMessage("Sure works for me", true, "11:23 AM"),
            XosChatMessage("See you tomorrow.", false, "11:24 AM")
        )
    ),
    XosConversation(
        2, "Nova", "nova@x0s",
        "https://api.dicebear.com/8.x/notionists/svg?seed=Nova&backgroundColor=0a0a0a",
        "Sent you a photo", "10:08 AM", unread = true, online = false,
        mutableListOf(
            XosChatMessage("Check this out", false, "10:05 AM"),
            XosChatMessage("Photo", false, "10:08 AM", isImage = true)
        )
    ),
    XosConversation(
        3, "Jay", "jay@x0s",
        "https://api.dicebear.com/8.x/notionists/svg?seed=Jay&backgroundColor=0d0d0d",
        "That was insane!", "9:47 AM", unread = false, online = true,
        mutableListOf(
            XosChatMessage("Did you see the match?", false, "9:45 AM"),
            XosChatMessage("Yes bro! unreal finish", true, "9:46 AM"),
            XosChatMessage("That was insane!", false, "9:47 AM")
        )
    ),
    XosConversation(
        4, "Lucas", "lucas@x0s",
        "https://api.dicebear.com/8.x/notionists/svg?seed=Lucas&backgroundColor=080808",
        "Let's catch up soon.", "8:31 AM", unread = true, online = false,
        mutableListOf(
            XosChatMessage("Haven't seen you in a while", false, "8:28 AM"),
            XosChatMessage("I know right, been busy", true, "8:30 AM"),
            XosChatMessage("Let's catch up soon.", false, "8:31 AM")
        )
    ),
    XosConversation(
        5, "Mila", "mila@x0s",
        "https://api.dicebear.com/8.x/notionists/svg?seed=Mila&backgroundColor=0f0f0f",
        "Okay, sounds good.", "7:12 AM", unread = false, online = false,
        mutableListOf(
            XosChatMessage("Can we reschedule the call?", true, "7:10 AM"),
            XosChatMessage("Okay, sounds good.", false, "7:12 AM")
        )
    )
)
