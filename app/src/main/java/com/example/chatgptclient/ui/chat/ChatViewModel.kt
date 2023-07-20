package com.example.chatgptclient.ui.chat

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatgptclient.logic.Repository
import com.example.chatgptclient.logic.model.Chat
import com.example.chatgptclient.logic.model.Msg
import kotlinx.coroutines.launch

class ChatViewModel: ViewModel() {

    private val addNewChatResultLiveData = MutableLiveData<Result<Long>>()

    private val renameChatNameResultLiveData = MutableLiveData<Result<Int>>()

    private val deleteChatResultLiveData = MutableLiveData<Result<Int>>()

    private val loadAllChatsLiveData = MutableLiveData<Any?>()

    private val loadMsgsOfChatLiveData = MutableLiveData<Long>()

    private val deleteMsgsOfChatLiveData = MutableLiveData<Any?>()

    private val addMsgsOfNewChatLiveData = MutableLiveData<Any?>()

    private val nonUpdateMsgListLiveData = MutableLiveData<Any?>()

    private val clearAllChatsLiveData = MutableLiveData<Any?>()

    private val clearAllMsgsLiveData = MutableLiveData<Any?>()

    private val nonUpdateMsgList = listOf(Msg("nonUpdateMsgList",Msg.TYPE_RECEIVED))

    var chatName = "ChatGPT"

    var isChatGPT = true

    var sendStateBeforeStop = true

    companion object {
        var curChatId: Long? = null
    }

    val chatsLiveData = MediatorLiveData<Result<List<Chat>>>().apply {
        addSource(addNewChatResultLiveData) { result ->
            if (result.isSuccess) {
                curChatId = result.getOrNull()
                viewModelScope.launch {
                    value = Repository.loadAllChats()
                }
            }
        }
        addSource(renameChatNameResultLiveData) { result ->
            if (result.isSuccess) {
                viewModelScope.launch {
                    value = Repository.loadAllChats()
                }
            }
        }
        addSource(deleteChatResultLiveData) { result ->
            if (result.isSuccess) {
                viewModelScope.launch {
                    value = Repository.loadAllChats()
                }
            }
        }
        addSource(loadAllChatsLiveData) {
            viewModelScope.launch {
                value = Repository.loadAllChats()
            }
        }
        addSource(clearAllChatsLiveData) {
            viewModelScope.launch {
                value = Repository.loadAllChats()
            }
        }
    }

    val msgsLiveData = MediatorLiveData<Result<List<Msg>>>().apply {
        addSource(loadMsgsOfChatLiveData) { chatId ->
            viewModelScope.launch {
                value = Repository.loadMsgsOfChat(chatId)
            }
        }
        addSource(deleteMsgsOfChatLiveData) {
            value = Result.success(emptyList())
        }
        addSource(addMsgsOfNewChatLiveData) {
            value = Result.success(emptyList())
        }
        addSource(clearAllMsgsLiveData) {
            value = Result.success(emptyList())
        }
        addSource(nonUpdateMsgListLiveData) {
            value = Result.success(nonUpdateMsgList)
        }
    }

    fun addNewChat() {
        viewModelScope.launch {
            val result = Repository.addNewChat(Chat("New chat"))
            addNewChatResultLiveData.value = result
        }
    }

    fun renameChatName(chatId: Long, chatName: String) {
        viewModelScope.launch {
            val result = Repository.renameChatName(chatId,chatName)
            renameChatNameResultLiveData.value = result
        }
    }

    fun deleteChat(chatId: Long) {
        viewModelScope.launch {
            val result = Repository.deleteChat(chatId)
            deleteChatResultLiveData.value = result
        }
    }

    fun loadAllChats(){
        loadAllChatsLiveData.value = loadAllChatsLiveData.value
    }

    fun loadMsgsOfChat(chatId: Long) {
        loadMsgsOfChatLiveData.value = chatId
    }

    fun addMsg(msg: Msg) {
        viewModelScope.launch {
            Repository.addMsg(msg)
        }
    }

    fun deleteMsgsOfChat(chatId: Long) {
        viewModelScope.launch {
            Repository.deleteMsgsOfChat(chatId)
            deleteMsgsOfChatLiveData.value = deleteMsgsOfChatLiveData.value
        }
    }

    fun addMsgsOfNewChat() {
        addMsgsOfNewChatLiveData.value = addMsgsOfNewChatLiveData.value
    }

    fun addNewChatAndMsg(msgStr: String) {
        viewModelScope.launch {
            val result = Repository.addNewChat(Chat("New chat"))
            addNewChatResultLiveData.value = result
            if (result.isSuccess) {
                val chatId = result.getOrNull()
                val msg = Msg(msgStr, Msg.TYPE_SENT, chatId)
                Repository.addMsg(msg)
            }
        }
    }

    fun clearAllChatsAndMsgs() {
        viewModelScope.launch {
            Repository.clearAllChatsAndMsgs()
            clearAllChatsLiveData.value = clearAllChatsLiveData.value
            clearAllMsgsLiveData.value = clearAllMsgsLiveData.value
        }
    }

    fun nonUpdateMsgList() {
        nonUpdateMsgListLiveData.value = nonUpdateMsgListLiveData.value
    }

    fun closeScope() {
        Repository.closeScope()
    }

}