package com.example.chatgptclient.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class Chat(var chatName: String) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}