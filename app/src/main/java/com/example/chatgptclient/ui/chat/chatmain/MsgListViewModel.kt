package com.example.chatgptclient.ui.chat.chatmain

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.example.chatgptclient.logic.Repository
import com.example.chatgptclient.logic.model.Msg
import com.example.chatgptclient.ui.chat.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

@OptIn(BetaOpenAI::class)
class MsgListViewModel: ViewModel() {

    private val _msgContentResult = MutableStateFlow<Result<String>?>(null)

    private val msgContentSB = StringBuilder()

    private var count = 0

    var isSend = MutableLiveData<Boolean>()

    val msgList = ArrayList<Msg>()

    val msgContentResult: StateFlow<Result<String>?> = _msgContentResult.asStateFlow()

    init {
        isSend.value = true
    }
    fun sendMessage(message: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val sendMsgsList = getSendMsgsList(message)
                Repository.getChatCompletions(sendMsgsList)
                    .catch { e ->
                        _msgContentResult.value = Result.failure(e)
                        isSend.postValue(true)
                    }
                    .collect { chatCompletionChunk ->
                        chatCompletionChunk.choices[0].delta?.let {
                            if (it.role != null) {
                                val msg = Msg("", Msg.TYPE_RECEIVED, ChatViewModel.curChatId)
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
                            Repository.addMsg(Msg(msgContentSB.toString(), Msg.TYPE_RECEIVED, ChatViewModel.curChatId))
                        }
                    }
            }
        }
    }

    /**
     * 获取当前需要发送消息及历史消息
     */
    private fun getSendMsgsList(message: String): List<ChatMessage> {
        val sendChatMessage = ChatMessage(
            role = ChatRole.User,
            content = message)
        val sendMsgsList = mutableListOf(sendChatMessage)
        var i = msgList.size - 2
        var j = 2
        loop@ while (i > 0 && j > 0) {
            while (msgList[i].type == Msg.TYPE_SENT) {
                i--
                if (i < 0) break@loop
            }
            if (msgList[i].type == Msg.TYPE_RECEIVED) {
                sendMsgsList.add(ChatMessage(
                    role = ChatRole.Assistant,
                    content = msgList[i].content
                ))
                sendMsgsList.add(
                    ChatMessage(
                        role = ChatRole.User,
                        content = msgList[i-1].content
                    ))
                i -= 2
                j--
            }
        }
        sendMsgsList.reverse()
        return sendMsgsList
    }
}