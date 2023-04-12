package com.example.chatgptclient.ui.chat.chatlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptclient.R
import com.example.chatgptclient.logic.model.Chat
import com.example.chatgptclient.ui.chat.ChatActivity

class ChatAdapter(private val chatActivity: ChatActivity, private val chatList: List<Chat>): RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val chatName: TextView = view.findViewById(R.id.chatName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item,parent,false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            if (chatActivity.chatMainViewModel.isSend) {
                val position = holder.bindingAdapterPosition
                val chat = chatList[position]
                chatActivity.topAppBar.title = chat.chatName
                chatActivity.chatMainViewModel.chatId = chat.id
                chatActivity.chatMainViewModel.isChatGPT = false
                chatActivity.chatMainViewModel.loadMsgs(chat.id)
                chatActivity.drawerLayout.closeDrawers()
            }
        }
        return holder
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position]
        holder.chatName.text = chat.chatName
    }

}