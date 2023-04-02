package com.example.chatgptclient.ui.chat

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aallam.openai.api.BetaOpenAI
import com.example.chatgptclient.R
import com.example.chatgptclient.logic.model.Msg
import com.example.chatgptclient.ui.chat.chatmain.ChatMainViewModel
import com.example.chatgptclient.ui.chat.chatmain.MsgAdapter
import com.google.android.material.appbar.MaterialToolbar
import io.noties.markwon.*
import io.noties.prism4j.Prism4j.*


class ChatActivity : AppCompatActivity() {
    private lateinit var topAppBar: MaterialToolbar

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var editTextMsg: EditText

    private lateinit var sendMsg: Button

    private lateinit var msgAdapter: MsgAdapter


    private lateinit var msgRecyclerView: RecyclerView

    val chatMainViewModel by lazy { ViewModelProvider(this).get(ChatMainViewModel::class.java) }

    @SuppressLint("MissingInflatedId")
    @OptIn(BetaOpenAI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        topAppBar = findViewById(R.id.topAppBar)
        drawerLayout = findViewById(R.id.drawerLayout)
        editTextMsg = findViewById(R.id.et_msg)
        sendMsg = findViewById(R.id.send_msg)
        msgRecyclerView = findViewById(R.id.rv_msg)

        val layoutManager = LinearLayoutManager(this)
        msgAdapter = MsgAdapter(chatMainViewModel.msgList)
        msgRecyclerView.layoutManager = layoutManager
        msgRecyclerView.adapter = msgAdapter

        if (isDarkTheme()) {
            topAppBar.setNavigationIcon(R.drawable.ic_chat_dark)
        } else {
            topAppBar.setNavigationIcon(R.drawable.ic_chat)
        }

        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        topAppBar.setOnMenuItemClickListener {menuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    true
                }
                R.id.settings -> {
                    true
                }
                else -> false
            }
        }
        editTextMsg.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()) {
                sendMsg.setBackgroundColor(getColor(R.color.light_blue))
            } else {
                if (isDarkTheme()) {
                    sendMsg.setBackgroundColor(getColor(R.color.black))
                }
                else {
                    sendMsg.setBackgroundColor(getColor(R.color.white_smoke))
                }
            }
        }
        sendMsg.setOnClickListener {
            val sendMsgStr = editTextMsg.text.toString()
            if (sendMsgStr.isNotEmpty()) {
                val msg = Msg(sendMsgStr, Msg.TYPE_SENT)
                chatMainViewModel.msgList.add(msg)
                msgAdapter.notifyItemInserted(chatMainViewModel.msgList.size -1 )
                msgRecyclerView.scrollToPosition(chatMainViewModel.msgList.size - 1)
                editTextMsg.text.clear()
                chatMainViewModel.sendMessage(sendMsgStr)
            }
        }

        chatMainViewModel.chatCompletionLiveData.observe(this, Observer { result ->
            val chatCompletion = result.getOrNull()
            if (chatCompletion != null) {
                chatCompletion.choices[0].message?.let {
                    Log.d("linhao",it.content)
                    val msg = Msg(it.content,Msg.TYPE_RECEIVED)
                    chatMainViewModel.msgList.add(msg)
                    msgAdapter.notifyItemInserted(chatMainViewModel.msgList.size -1 )
                    msgRecyclerView.scrollToPosition(chatMainViewModel.msgList.size - 1)
                }
            } else {
                Toast.makeText(this,"请求失败",Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })

    }

    /**
     * 判断是否是深色主题
     */
    private fun isDarkTheme(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}