package com.example.chatgptclient.ui.settings

import androidx.lifecycle.ViewModel
import com.example.chatgptclient.logic.Repository

class SettingsViewModel: ViewModel() {

    fun setApiKey(apiKey: String) {
        Repository.setApiKey(apiKey)
    }

    fun setMulTurnCon(enable: Boolean) {
        Repository.setMulTurnCon(enable)
    }

    fun setTem(tem: Int) {
        Repository.setTem(tem)
    }
}