package com.example.chatgptclient.logic

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.example.chatgptclient.ChatGPTClientApplication
import com.example.chatgptclient.logic.dao.SettingsDao
import com.example.chatgptclient.logic.model.Chat
import com.example.chatgptclient.logic.model.Msg
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.seconds

object Repository {

    private var apiKey: String = ""

    private var isMulTurnCon: Boolean = false

    private var temInt: Int = 10

    private var temDouble: Double? = null

    private var config: OpenAIConfig? = null

    private var openAI: OpenAI? = null

    private val chatDao = AppDatabase.getDatabase(ChatGPTClientApplication.context).chatDao()

    private val msgDao = AppDatabase.getDatabase(ChatGPTClientApplication.context).msgDao()

    private val scope = CoroutineScope(Dispatchers.IO)

    private var job: Job? = null

    init {
        job = scope.launch {
            val deferredApiKey = async { SettingsDao.getApiKey() }
            val deferredIsMulTurnCon = async { SettingsDao.getIsMultiTurnCon() }
            val deferredTemInt = async { SettingsDao.getTemInt() }
            apiKey = deferredApiKey.await()
            isMulTurnCon = deferredIsMulTurnCon.await()
            temInt = deferredTemInt.await()
            temDouble = temInt.toDouble() / 10
            config = OpenAIConfig(token = apiKey, timeout = Timeout(socket = 60.seconds))
            openAI = OpenAI(config!!)
        }
    }

    fun closeScope() {
        job?.cancel()
    }

    fun setApiKey(apiKey: String) {
        config = OpenAIConfig(token = apiKey, timeout = Timeout(socket = 60.seconds))
        openAI = OpenAI(config!!)
    }

    fun setMulTurnCon(enable: Boolean) {
        isMulTurnCon = enable
    }

    fun setTem(tem: Int) {
        temDouble = tem.toDouble()/10
    }

    @OptIn(BetaOpenAI::class)
    fun getChatCompletions(sendMsgs: List<ChatMessage>): Flow<ChatCompletionChunk> {
        val model = ModelId("gpt-3.5-turbo")
        val messages = if (isMulTurnCon) {
            sendMsgs
        } else {
            listOf(sendMsgs.last())
        }
        val chatCompletionRequest = ChatCompletionRequest(
            model,
            messages,
            temDouble
        )
        return openAI!!.chatCompletions(chatCompletionRequest)
    }

    suspend fun addNewChat(chat: Chat): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val num = async { chatDao.insertChat(chat) }.await()
                Result.success(num)
            }
        } catch (e: Exception) {
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