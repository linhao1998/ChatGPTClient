package com.example.chatgptclient.logic

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.chatgptclient.ChatGPTClientApplication
import com.example.chatgptclient.logic.dao.SettingsDao
import com.example.chatgptclient.logic.model.Chat
import com.example.chatgptclient.logic.model.Msg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

object Repository {

    private var apiKey = SettingsDao.getApiKey()

    private var isMultiTurnCon = SettingsDao.getIsMultiTurnCon()

    private var temInt: Int = SettingsDao.getTemInt()

    private var temDouble: Double = temInt.toDouble()/10

    private var openAI = OpenAI(apiKey)

    private val chatDao = AppDatabase.getDatabase(ChatGPTClientApplication.context).chatDao()

    private val msgDao = AppDatabase.getDatabase(ChatGPTClientApplication.context).msgDao()

    fun resetOpenAI(apiKey: String) {
        openAI = OpenAI(apiKey)
    }

    fun resetIsMultiTurnCon(enable: Boolean) {
        isMultiTurnCon = enable
    }

    fun resetTem(tem: Int) {
        temDouble = tem.toDouble()/10
    }

    @OptIn(BetaOpenAI::class)
    fun getChatCompletions(contentStr: String, preUser: String, preAssistant: String): Flow<ChatCompletionChunk> {
        val model = ModelId("gpt-3.5-turbo")
        if (isMultiTurnCon) {
            val chatCompletionRequest = ChatCompletionRequest(
                model,
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.User,
                        content = preUser
                    ),
                    ChatMessage(
                        role = ChatRole.Assistant,
                        content = preAssistant
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = contentStr
                    )
                ),
                temDouble
            )
            return openAI.chatCompletions(chatCompletionRequest)
        } else {
            val chatCompletionRequest = ChatCompletionRequest(
                model,
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.User,
                        content = contentStr
                    )
                ),
                temDouble
            )
            return openAI.chatCompletions(chatCompletionRequest)
        }
    }

    suspend fun addNewChat(chat: Chat): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val num = async { chatDao.insertChat(chat) }.await()
                Result.success(num)
            }
        }catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameChatName(chatId: Long, chatName: String): Result<Int> {
        return try {
            withContext(Dispatchers.IO) {
                val num = async { chatDao.updateChatName(chatId,chatName) }.await()
                Result.success(num)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteChat(chatId: Long): Result<Int> {
        return try {
            withContext(Dispatchers.IO) {
                val num = async { chatDao.deleteChatByChatId(chatId) }.await()
                Result.success(num)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadAllChats(): Result<List<Chat>>{
        return try {
            withContext(Dispatchers.IO) {
                val chatList = async { chatDao.loadAllChats() }.await()
                Result.success(chatList)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMsg(msg: Msg) {
        try {
            withContext(Dispatchers.IO) {
                msgDao.insertMsg(msg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteMsgsOfChat(chatId: Long): Result<Int> {
        return try {
            withContext(Dispatchers.IO) {
                val num = async { msgDao.deleteMessagesByChatId(chatId) }.await()
                Result.success(num)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadMsgsOfChat(chatId: Long): Result<List<Msg>> {
        return try {
            withContext(Dispatchers.IO) {
                val msgList = async { msgDao.loadMsgs(chatId) }.await()
                Result.success(msgList)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllChatsAndMsgs() {
        try {
            withContext(Dispatchers.IO) {
                val deferredChats = async { chatDao.deleteAllChats() }
                val deferredMessages = async { msgDao.deleteAllMessages() }
                deferredChats.await()
                deferredMessages.await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}