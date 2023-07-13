package com.example.chatgptclient.ui.chat.chatlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptclient.R
import com.example.chatgptclient.logic.model.Chat
import com.example.chatgptclient.ui.chat.ChatActivity
import com.example.chatgptclient.ui.chat.ChatViewModel

class ChatAdapter(private val chatActivity: ChatActivity, private val chatList: List<Chat>): RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val chatName: TextView = view.findViewById(R.id.chatName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item,parent,false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            if (chatActivity.msgListViewModel.isSend.value == true) {
                val position = holder.bindingAdapterPosition
                val chat = chatList[position]
                chatActivity.chatViewModel.chatName = chat.chatName
                chatActivity.topAppBar.title = chatActivity.chatViewModel.chatName
                ChatViewModel.curChatId = chat.id
                chatActivity.chatViewModel.isChatGPT = false
                chatActivity.msgRecyclerView.visibility = View.VISIBLE
                chatActivity.bgTextView.visibility = View.GONE
                chatActivity.chatViewModel.loadMsgsOfChat(chat.id)
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