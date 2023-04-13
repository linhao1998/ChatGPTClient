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
import com.example.chatgptclient.ui.chat.chatlist.ChatAdapter
import com.example.chatgptclient.ui.chat.chatlist.ChatListViewModel
import com.example.chatgptclient.ui.chat.chatmain.ChatMainViewModel
import com.example.chatgptclient.ui.chat.chatmain.MsgAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    lateinit var topAppBar: MaterialToolbar

    lateinit var drawerLayout: DrawerLayout

    private lateinit var editTextMsg: EditText

    private lateinit var sendMsg: Button

    private lateinit var msgAdapter: MsgAdapter

    private lateinit var chatAdapter: ChatAdapter

    private lateinit var msgRecyclerView: RecyclerView

    private lateinit var chatRecyclerView: RecyclerView

    private lateinit var addNewChatBtn: Button

    private lateinit var sendMsgStr: String

    val chatMainViewModel by lazy { ViewModelProvider(this).get(ChatMainViewModel::class.java) }

    val chatListViewModel by lazy { ViewModelProvider(this).get(ChatListViewModel::class.java) }

    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        topAppBar = findViewById(R.id.topAppBar)
        drawerLayout = findViewById(R.id.drawerLayout)
        editTextMsg = findViewById(R.id.et_msg)
        sendMsg = findViewById(R.id.send_msg)
        msgRecyclerView = findViewById(R.id.rv_msg)
        chatRecyclerView = findViewById(R.id.rv_chat)
        addNewChatBtn = findViewById(R.id.addNewChatBtn)

        val msgLayoutManager = LinearLayoutManager(this)
        msgAdapter = MsgAdapter(chatMainViewModel.msgList)
        msgRecyclerView.layoutManager = msgLayoutManager
        msgRecyclerView.adapter = msgAdapter

        val chatLayoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(this,chatListViewModel.chatList)
        chatRecyclerView.layoutManager = chatLayoutManager
        chatRecyclerView.adapter = chatAdapter

        if (isDarkTheme()) {
            topAppBar.setNavigationIcon(R.drawable.ic_chat_dark)
        } else {
            topAppBar.setNavigationIcon(R.drawable.ic_chat)
        }

        chatListViewModel.refreshChatList()

        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        topAppBar.setOnMenuItemClickListener {menuItem ->
            when (menuItem.itemId) {
                R.id.rename -> {
                    if (!chatMainViewModel.isChatGPT) {
                        showRenameDialog()
                    }
                    true
                }
                R.id.delete -> {
                    true
                }
                R.id.copy -> {
                    copyResponse()
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
            sendMsgStr = editTextMsg.text.toString()
            if (sendMsgStr.isNotEmpty() && chatMainViewModel.isSend) {
                if (chatMainViewModel.chatId == null) {
                    chatListViewModel.addChat()
                } else {
                    chatMainViewModel.isSend = false
                    val msg = Msg(sendMsgStr, Msg.TYPE_SENT, chatMainViewModel.chatId)
                    chatMainViewModel.msgList.add(msg)
                    chatMainViewModel.addMsg(msg)
                    msgAdapter.notifyItemInserted(chatMainViewModel.msgList.size -1 )
                    msgRecyclerView.scrollToPosition(chatMainViewModel.msgList.size - 1)
                    editTextMsg.text.clear()
                    chatMainViewModel.sendMessage(sendMsgStr)
                }
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
        addNewChatBtn.setOnClickListener {
            if (chatMainViewModel.isSend) {
                chatListViewModel.addChat()
                chatMainViewModel.isChatGPT = false
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
               chatMainViewModel.msgContentResult.collect { result ->
                   if (result != null) {
                       val msgContent = result.getOrNull()
                       if (msgContent != null) {
                           chatMainViewModel.msgList[chatMainViewModel.msgList.size - 1].content = msgContent
                           msgAdapter.notifyItemChanged(chatMainViewModel.msgList.size - 1)
                           val lastVisibleItemPosition = msgLayoutManager.findLastVisibleItemPosition()
                           val lastItem = msgLayoutManager.findViewByPosition(lastVisibleItemPosition)
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

        chatListViewModel.addChatLiveData.observe(this) { result ->
            val chatId = result.getOrNull()
            if (chatId != null) {
                chatMainViewModel.chatId = chatId
                topAppBar.title = "New chat"
                drawerLayout.closeDrawers()
                chatListViewModel.refreshChatList()
                chatMainViewModel.msgList.clear()
                msgAdapter.notifyDataSetChanged()

                if (chatMainViewModel.isChatGPT) {
                    chatMainViewModel.isSend = false
                    chatMainViewModel.isChatGPT = false
                    val msg = Msg(sendMsgStr, Msg.TYPE_SENT, chatMainViewModel.chatId)
                    chatMainViewModel.msgList.add(msg)
                    chatMainViewModel.addMsg(msg)
                    msgAdapter.notifyItemInserted(chatMainViewModel.msgList.size -1 )
                    msgRecyclerView.scrollToPosition(chatMainViewModel.msgList.size - 1)
                    editTextMsg.text.clear()
                    chatMainViewModel.sendMessage(sendMsgStr)
                }
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }
        chatListViewModel.loadAllChats.observe(this) { result ->
            val chats = result.getOrNull()
            if (chats != null) {
                chatListViewModel.chatList.clear()
                chatListViewModel.chatList.addAll(chats)
                chatAdapter.notifyDataSetChanged()
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }
        chatMainViewModel.loadMsgsLiveData.observe(this) { result ->
            val msgs = result.getOrNull()
            if (msgs != null) {
                chatMainViewModel.msgList.clear()
                chatMainViewModel.msgList.addAll(msgs)
                msgAdapter.notifyDataSetChanged()
                msgRecyclerView.scrollToPosition(chatMainViewModel.msgList.size - 1)
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }
        chatMainViewModel.renameChatNameLiveData.observe(this) { result ->
            val chats = result.getOrNull()
            if (chats != null) {
                chatListViewModel.chatList.clear()
                chatListViewModel.chatList.addAll(chats)
                chatAdapter.notifyDataSetChanged()
            } else {
                result.exceptionOrNull()?.printStackTrace()
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

    /**
     * 显示重命名对话框
     */
    private fun showRenameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_rename,null)
        val editTextRename: EditText = dialogView.findViewById(R.id.et_rename)
        MaterialAlertDialogBuilder(this)
            .setTitle("重命名")
            .setView(dialogView)
            .setNegativeButton("取消") { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton("确定") { dialog, which ->
                val text = editTextRename.text.toString()
                topAppBar.title = text
                chatMainViewModel.renameChatName(text)
                dialog.dismiss()
            }
            .show()
        editTextRename.setText(topAppBar.title)
        editTextRename.requestFocus()
    }

    /**
     * 复制回复到剪切板
     */
    private fun copyResponse() {
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
    }

}