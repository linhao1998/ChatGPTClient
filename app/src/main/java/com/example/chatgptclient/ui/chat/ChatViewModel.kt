package com.example.chatgptclient.ui.chat

import androidx.lifecycle.*
import com.example.chatgptclient.logic.Repository
import com.example.chatgptclient.logic.model.Chat
import com.example.chatgptclient.logic.model.Msg
import kotlinx.coroutines.launch

class ChatViewModel: ViewModel() {

    private val refreshChatsLiveData = MutableLiveData<Any?>()

    private val loadMsgsOfChatLiveData = MutableLiveData<Long>()

    private val clearMsgsLiveData = MutableLiveData<Any?>()

    private val nonUpdateMsgListLiveData = MutableLiveData<Any?>()

    private val nonUpdateMsgList = listOf(Msg("nonUpdateMsgList", Msg.TYPE_RECEIVED))

    private val _toastLiveData = MutableLiveData<String>()

    val toastLiveData: LiveData<String>
        get() = _toastLiveData

    var chatName = "ChatGPT"

    var isChatGPT = true

    var sendStateBeforeStop = true

    companion object {
        var curChatId: Long? = null
    }

    val chatsLiveData = refreshChatsLiveData.switchMap { _ ->
        Repository.loadAllChats()
    }

    val msgsLiveData = MediatorLiveData<Result<List<Msg>>>().apply {
        addSource(loadMsgsOfChatLiveData) { chatId ->
            viewModelScope.launch {
                value = Repository.loadMsgsOfChat(chatId)
            }
        }
        addSource(clearMsgsLiveData) {
            value = Result.success(emptyList())
        }
        addSource(nonUpdateMsgListLiveData) {
            value = Result.success(nonUpdateMsgList)
        }
    }

    fun addNewChat() {
        viewModelScope.launch {
            val result = Repository.addNewChat(Chat("New chat"))
            if (result.isSuccess) {
                refreshChatsLiveData.value = refreshChatsLiveData.value
                clearMsgsLiveData.value = clearMsgsLiveData.value
                curChatId = result.getOrNull()
            } else {
                _toastLiveData.value = "新增对话失败"
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun addNewChatAndMsg(msgStr: String) {
        viewModelScope.launch {
            val addNewChatResult = Repository.addNewChat(Chat("New chat"))
            if (addNewChatResult.isSuccess) {
                refreshChatsLiveData.value = refreshChatsLiveData.value
                val chatId = addNewChatResult.getOrNull()
                curChatId = chatId
                val msg = Msg(msgStr, Msg.TYPE_SENT, chatId)
                val addMsgResult = Repository.addMsg(msg)
                if (addMsgResult.isFailure) {
                    _toastLiveData.value = "添加消息失败"
                    addMsgResult.exceptionOrNull()?.printStackTrace()
                }
            } else {
                _toastLiveData.value = "新增对话失败"
                addNewChatResult.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun renameChatName(chatId: Long, chatName: String) {
        viewModelScope.launch {
            val result = Repository.renameChatName(chatId,chatName)
            if (result.isSuccess) {
                refreshChatsLiveData.value = refreshChatsLiveData.value
            } else {
                _toastLiveData.value = "重命名失败"
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun deleteChatAndMsgs(chatId: Long) {
        viewModelScope.launch {
            val result = Repository.deleteChatAndMsgs(chatId)
            if (result.isSuccess) {
                refreshChatsLiveData.value = refreshChatsLiveData.value
                clearMsgsLiveData.value = clearMsgsLiveData.value
            } else {
                _toastLiveData.value = "删除对话失败"
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun loadAllChats(){
        refreshChatsLiveData.value = refreshChatsLiveData.value
    }

    fun loadMsgsOfChat(chatId: Long) {
        loadMsgsOfChatLiveData.value = chatId
    }

    fun addMsg(msg: Msg) {
        viewModelScope.launch {
            val result = Repository.addMsg(msg)
            if (result.isFailure) {
                _toastLiveData.value = "添加消息失败"
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun clearAllChatsAndMsgs() {
        viewModelScope.launch {
            val result = Repository.clearAllChatsAndMsgs()
            if (result.isSuccess) {
                refreshChatsLiveData.value = refreshChatsLiveData
                clearMsgsLiveData.value = clearMsgsLiveData
            } else {
                _toastLiveData.value = "清除所有对话失败"
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun nonUpdateMsgList() {
        nonUpdateMsgListLiveData.value = nonUpdateMsgListLiveData.value
    }

    fun clearToast() {
        _toastLiveData.value = ""
    }

    fun closeScope() {
        Repository.closeScope()
    }

}