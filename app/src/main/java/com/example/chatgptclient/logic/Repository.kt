package com.example.chatgptclient.logic

import android.accounts.NetworkErrorException
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.flow.Flow

object Repository {

    private val apiKey: String = ""

    private val openAI = OpenAI(apiKey)

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
}