package com.example.chatgptclient.ui.chat.chatlist

import androidx.lifecycle.ViewModel
import com.example.chatgptclient.logic.model.Chat

class ChatListViewModel: ViewModel() {

    val chatList = ArrayList<Chat>()

}