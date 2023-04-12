package com.example.chatgptclient.logic

import androidx.lifecycle.liveData
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.chatgptclient.ChatGPTClientApplication
import com.example.chatgptclient.logic.model.Chat
import com.example.chatgptclient.logic.model.Msg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

object Repository {

    private val apiKey: String = ""

    private val openAI = OpenAI(apiKey)

    private val chatDao = AppDatabase.getDatabase(ChatGPTClientApplication.context).chatDao()

    private val msgDao = AppDatabase.getDatabase(ChatGPTClientApplication.context).msgDao()

    @OptIn(BetaOpenAI::class)
    fun getChatCompletions(contentStr: String): Flow<ChatCompletionChunk> {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = contentStr
                )
            )
        )
        return openAI.chatCompletions(chatCompletionRequest)
    }

    fun addChat(chat: Chat) = fire(Dispatchers.IO) {
        coroutineScope {
            val chatId = async { chatDao.insertChat(chat) }.await()
            Result.success(chatId)
        }
    }

    fun loadAllChats() = fire(Dispatchers.IO) {
        coroutineScope {
            val chatList = async { chatDao.loadAllChats() }.await()
            Result.success(chatList)
        }
    }

    fun loadMsgs(chatId: Long) = fire(Dispatchers.IO) {
        coroutineScope {
            val msgList = async { msgDao.loadMsgs(chatId) }.await()
            Result.success(msgList)
        }
    }

    fun addMsg(msg: Msg) {
        try {
            msgDao.insertMsg(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData(context) {
            val result = try {
                block()
            }catch (e: Exception) {
                Result.failure(e)
            }
            emit(result)
        }
}