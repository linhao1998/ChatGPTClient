package com.example.chatgptclient.ui.chat.chatmain

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.aallam.openai.api.BetaOpenAI
import com.example.chatgptclient.logic.Repository

class ChatMainViewModel: ViewModel() {

    private val messageLiveData = MutableLiveData<String>()

    @OptIn(BetaOpenAI::class)
    val chatCompletionLiveData = Transformations.switchMap(messageLiveData) { message ->
        Repository.getChatCompletion(message)
    }

    fun sendMessage(message: String) {
        messageLiveData.value = message
    }

}