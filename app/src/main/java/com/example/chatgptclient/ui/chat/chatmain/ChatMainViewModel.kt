package com.example.chatgptclient.ui.chat.chatmain

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.aallam.openai.api.BetaOpenAI
import com.example.chatgptclient.logic.Repository
import com.example.chatgptclient.logic.model.Msg

class ChatMainViewModel: ViewModel() {

    private val messageLiveData = MutableLiveData<String>()

    val msgList = ArrayList<Msg>()

    @OptIn(BetaOpenAI::class)
    val chatCompletionLiveData = Transformations.switchMap(messageLiveData) { message ->
        Repository.getChatCompletion(message)
    }

    fun sendMessage(message: String) {
        messageLiveData.value = message
    }

}