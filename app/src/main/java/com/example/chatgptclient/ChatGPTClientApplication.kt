package com.example.chatgptclient

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class ChatGPTClientApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var sharedPreferences: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }
}