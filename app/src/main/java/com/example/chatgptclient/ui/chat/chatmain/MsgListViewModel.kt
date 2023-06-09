package com.example.chatgptclient.ui.chat.chatmain

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.BetaOpenAI
import com.example.chatgptclient.logic.Repository
import com.example.chatgptclient.logic.model.Msg
import com.example.chatgptclient.ui.chat.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(BetaOpenAI::class)
class MsgListViewModel: ViewModel() {

    private val _msgContentResult = MutableStateFlow<Result<String>?>(null)

    private val msgContentSB = StringBuilder()

    private var count = 0

    var isSend = MutableLiveData<Boolean>()

    init {
        isSend.value = true
    }

    companion object {
        val msgList = ArrayList<Msg>()
    }

    val msgContentResult: StateFlow<Result<String>?> = _msgContentResult.asStateFlow()

    fun sendMessage(message: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var preUser = ""
                var preAssistant = ""
                if (msgList.size >=3 && msgList[msgList.size-2].type == Msg.TYPE_RECEIVED) {
                    preAssistant = msgList[msgList.size-2].content
                    preUser = msgList[msgList.size-3].content
                }
                Repository.getChatCompletions(message,preUser,preAssistant)
                    .catch { e ->
                        _msgContentResult.value = Result.failure(e)
                        isSend.postValue(true)
                    }
                    .collect { chatCompletionChunk ->
                        chatCompletionChunk.choices[0].delta?.let {
                            if (it.role != null) {
                                val msg = Msg("", Msg.TYPE_RECEIVED, ChatViewModel.chatId)
                                msgList.add(msg)
                                msgContentSB.clear()
                                count = 0
                            } else {
                                count++
                                val content = it.content ?: ""
                                msgContentSB.append(content)
                                if (count == 5 || count == 25 || count == 50 || count % 80 == 0 || content == "") {
                                    _msgContentResult.value = Result.success(msgContentSB.toString())
                                }
                            }
                        }
                        if (chatCompletionChunk.choices[0].finishReason == "stop") {
                            isSend.postValue(true)
                            Repository.addMsg(Msg(msgContentSB.toString(),Msg.TYPE_RECEIVED,ChatViewModel.chatId))
                        }
                    }
            }
        }
    }
}