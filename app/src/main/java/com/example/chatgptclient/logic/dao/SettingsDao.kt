package com.example.chatgptclient.logic.dao

import com.example.chatgptclient.ChatGPTClientApplication

object SettingsDao {

    fun getApiKey(): String {
        return ChatGPTClientApplication.sharedPreferences.getString("api_key","") ?: ""
    }

    fun getIsMultiTurnCon(): Boolean {
        return ChatGPTClientApplication.sharedPreferences.getBoolean("enable_continuous_conversation",false)
    }

    fun getTemInt(): Int {
        return ChatGPTClientApplication.sharedPreferences.getInt("temperature",10)
    }
}