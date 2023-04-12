package com.example.chatgptclient.ui.chat.chatlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.chatgptclient.logic.Repository
import com.example.chatgptclient.logic.model.Chat

class ChatListViewModel: ViewModel() {

    private val chatLiveData = MutableLiveData<Chat>()

    private val refreshLiveData = MutableLiveData<Any?>()

    val chatList = ArrayList<Chat>()

    val addChatLiveData = chatLiveData.switchMap { chat ->
        Repository.addChat(chat)
    }

    val loadAllChats = refreshLiveData.switchMap {
        Repository.loadAllChats()
    }

    fun addChat() {
        chatLiveData.value = Chat("New chat")
    }

    fun refreshChatList() {
        refreshLiveData.value = refreshLiveData.value
    }
}