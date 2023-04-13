package com.example.chatgptclient.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.chatgptclient.logic.model.Msg

@Dao
interface MsgDao {

    @Insert
    fun insertMsg(msg: Msg): Long

    @Query("select * from messages where chatId = :chatId")
    fun loadMsgs(chatId: Long): List<Msg>

    @Query("delete from messages where chatId = :chatId")
    fun deleteMessagesByChatId(chatId: Long): Int
}