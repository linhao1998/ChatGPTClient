package com.example.chatgptclient.logic.model

class Msg (var content: String, val type: Int) {
    companion object {
        const val TYPE_RECEIVED = 0
        const val TYPE_SENT = 1
    }
}