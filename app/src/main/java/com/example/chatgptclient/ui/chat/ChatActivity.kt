package com.example.chatgptclient.ui.chat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptclient.ChatGPTClientApplication
import com.example.chatgptclient.R
import com.example.chatgptclient.logic.model.Msg
import com.example.chatgptclient.ui.chat.chatmain.ChatMainViewModel
import com.example.chatgptclient.ui.chat.chatmain.MsgAdapter
import com.google.android.material.appbar.MaterialToolbar
import io.noties.markwon.*
import io.noties.prism4j.Prism4j.*
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var topAppBar: MaterialToolbar

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var editTextMsg: EditText

    private lateinit var sendMsg: Button

    private lateinit var msgAdapter: MsgAdapter

    private lateinit var msgRecyclerView: RecyclerView

    val chatMainViewModel by lazy { ViewModelProvider(this).get(ChatMainViewModel::class.java) }

    @SuppressLint("MissingInflatedId")
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
                R.id.rename -> {
                    true
                }
                R.id.copy -> {
                    if (chatMainViewModel.msgList.size >= 2) {
                        val msg = chatMainViewModel.msgList[chatMainViewModel.msgList.size-1]
                        if (msg.type == Msg.TYPE_RECEIVED) {
                            val clipboard =
                                ChatGPTClientApplication.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(null, msg.content))
                            Toast.makeText(
                                ChatGPTClientApplication.context,
                                "复制回复成功",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
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
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                drawerLayout.clearFocus()
            }

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerStateChanged(newState: Int) {}
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
               chatMainViewModel.msgContentResult.collect { result ->
                   if (result != null) {
                       val msgContent = result.getOrNull()
                       if (msgContent != null) {
                           chatMainViewModel.msgList[chatMainViewModel.msgList.size - 1].content = msgContent
                           msgAdapter.notifyItemChanged(chatMainViewModel.msgList.size - 1)
                           val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                           val lastItem = layoutManager.findViewByPosition(lastVisibleItemPosition)
                           val lastItemBottom = lastItem?.bottom ?: 0
                           msgRecyclerView.scrollBy(0, lastItemBottom)
                       } else {
                           Toast.makeText(ChatGPTClientApplication.context,"请求失败",Toast.LENGTH_SHORT).show()
                           result.exceptionOrNull()?.printStackTrace()
                       }
                   }
               }
            }
        }
    }

    /**
     * 判断是否是深色主题
     */
    private fun isDarkTheme(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}