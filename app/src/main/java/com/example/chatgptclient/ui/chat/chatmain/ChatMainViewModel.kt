package com.example.chatgptclient.ui.chat.chatmain

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.BetaOpenAI
import com.example.chatgptclient.logic.Repository
import com.example.chatgptclient.logic.model.Msg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(BetaOpenAI::class)
class ChatMainViewModel: ViewModel() {

    private val _msgContentResult = MutableStateFlow<Result<String>?>(null)

    private val msgContentSB = StringBuilder()

    private var count = 0

    private val chatIdLiveData = MutableLiveData<Long>()

    var chatId:Long? = null

    var isSend = 1

    var isChatGPT = 1

    val msgList = ArrayList<Msg>()

    val msgContentResult: StateFlow<Result<String>?> = _msgContentResult.asStateFlow()

    val loadMsgsLiveData = chatIdLiveData.switchMap { chatId ->
        Repository.loadMsgs(chatId)
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Repository.getChatCompletions(message)
                    .catch { e ->
                        _msgContentResult.value = Result.failure(e)
                        isSend = 1
                    }
                    .collect { chatCompletionChunk ->
//                        Log.d("linhao",chatCompletionChunk.toString())
                        chatCompletionChunk.choices[0].delta?.let {
                            if (it.role != null) {
                                val msg = Msg("", Msg.TYPE_RECEIVED, chatId)
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
                            isSend = 1
                            Repository.addMsg(Msg(msgContentSB.toString(),Msg.TYPE_RECEIVED,chatId))
                        }
                    }
            }
        }
    }

    fun loadMsgs(chatId: Long) {
        chatIdLiveData.value = chatId
    }

    fun addMsg(msg: Msg) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Repository.addMsg(msg)
            }
        }
    }
}