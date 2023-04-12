package com.example.chatgptclient.logic

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chatgptclient.logic.dao.ChatDao
import com.example.chatgptclient.logic.dao.MsgDao
import com.example.chatgptclient.logic.model.Chat
import com.example.chatgptclient.logic.model.Msg

@Database(version = 1, entities = [Chat::class, Msg::class])
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao

    abstract fun msgDao(): MsgDao

    companion object {

        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java,"app_database")
                .build().apply {
                    instance = this
                }
        }

    }

}