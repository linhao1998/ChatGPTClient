package com.example.chatgptclient.ui.settings

import androidx.lifecycle.ViewModel
import com.example.chatgptclient.logic.Repository

class SettingsViewModel: ViewModel() {

    fun resetOpenAI(apiKey: String) {
        Repository.resetOpenAI(apiKey)
    }

    fun resetIsMultiTurnCon(enable: Boolean) {
        Repository.resetIsMultiTurnCon(enable)
    }

    fun resetTem(tem: Int) {
        Repository.resetTem(tem)
    }
}