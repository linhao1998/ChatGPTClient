package com.example.chatgptclient.ui.chat.chatmain

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

    private val chatIdLiveData = MutableLiveData<Long>()

    private val chatNameLiveData = MutableLiveData<String>()

    private val deleteChatIdLiveData = MutableLiveData<Long>()

    private var count = 0

    var chatId:Long? = null

    var isSend = true

    var isChatGPT = true

    val msgList = ArrayList<Msg>()

    val msgContentResult: StateFlow<Result<String>?> = _msgContentResult.asStateFlow()

    val loadMsgsLiveData = chatIdLiveData.switchMap { chatId ->
        Repository.loadMsgs(chatId)
    }

    val renameChatNameLiveData = chatNameLiveData.switchMap { chatName ->
        Repository.renameChatName(chatId!!, chatName)
    }

    val deleteChatAndMsgLiveData = deleteChatIdLiveData.switchMap { chatId ->
        Repository.deleteChatAndMsgs(chatId)
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Repository.getChatCompletions(message)
                    .catch { e ->
                        _msgContentResult.value = Result.failure(e)
                        isSend = true
                    }
                    .collect { chatCompletionChunk ->
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
                            isSend = true
                            Repository.addMsg(Msg(msgContentSB.toString(),Msg.TYPE_RECEIVED,chatId))
                        }
                    }
            }
        }
    }

    fun addMsg(msg: Msg) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Repository.addMsg(msg)
            }
        }
    }

    fun loadMsgs(chatId: Long) {
        chatIdLiveData.value = chatId
    }

    fun renameChatName(chatName: String) {
        chatNameLiveData.value = chatName
    }

    fun deleteChatAndMsgs(chatId: Long) {
        deleteChatIdLiveData.value = chatId
    }
}