package com.example.chatgptclient.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.completion.TextCompletion
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.Flow

object Repository {

    private val apiKey: String = "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"

    private val openAI = OpenAI(apiKey)


    @OptIn(BetaOpenAI::class)
    fun getChatCompletion(contentStr: String) = liveData(Dispatchers.IO) {
        val result = try {
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.User,
                        content = contentStr
                    )
                )
            )
            coroutineScope {
                val completion: ChatCompletion = async { openAI.chatCompletion(chatCompletionRequest) }.await()
//                val completions: Flow<ChatCompletionChunk> = async { openAI.chatCompletions(chatCompletionRequest) }.await()
                Result.success(completion)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }
}