package com.example.chatgptclient.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.chatgptclient.logic.model.Chat

@Dao
interface ChatDao {

    @Insert
    fun insertChat(chat: Chat): Long

    @Query("select * from chats")
    fun loadAllChats(): List<Chat>

    @Query("update chats set chatName = :newChatName where id = :id")
    fun updateChatName(id: Long, newChatName: String): Int

    @Query("delete from chats where id = :chatId")
    fun deleteChatByChatId(chatId: Long): Int

}