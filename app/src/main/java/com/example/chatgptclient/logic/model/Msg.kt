package com.example.chatgptclient.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Msg (var content: String, val type: Int, var chatId: Long? = null) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    companion object {
        const val TYPE_RECEIVED = 0
        const val TYPE_SENT = 1
    }
}