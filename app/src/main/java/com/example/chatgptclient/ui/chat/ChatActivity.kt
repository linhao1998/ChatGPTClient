package com.example.chatgptclient.ui.chat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
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
import com.example.chatgptclient.ui.chat.chatmain.MsgListViewModel
import com.example.chatgptclient.ui.chat.chatmain.MsgAdapter
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var editTextMsg: EditText

    private lateinit var sendMsg: Button

    private lateinit var addNewChatBtn: Button

    private lateinit var clearAllChatsBtn: Button

    private lateinit var msgAdapter: MsgAdapter

    private lateinit var chatAdapter: ChatAdapter

    private lateinit var chatRecyclerView: RecyclerView

    private lateinit var loadingIndicator: SpinKitView

    lateinit var msgRecyclerView: RecyclerView

    lateinit var textViewBg: TextView

    lateinit var topAppBar: MaterialToolbar

    lateinit var drawerLayout: DrawerLayout

    val msgListViewModel by lazy { ViewModelProvider(this)[MsgListViewModel::class.java] }

    private val chatListViewModel by lazy { ViewModelProvider(this)[ChatListViewModel::class.java] }

    val chatViewModel by lazy { ViewModelProvider(this)[ChatViewModel::class.java] }

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
        textViewBg = findViewById(R.id.tv_bg)
        loadingIndicator = findViewById(R.id.loading)
        clearAllChatsBtn = findViewById(R.id.clearAllChatsBtn)

        if (isDarkTheme()) {
            topAppBar.setNavigationIcon(R.drawable.ic_chat_dark)
        } else {
            topAppBar.setNavigationIcon(R.drawable.ic_chat)
        }
        if (!chatViewModel.isChatGPT) {
            msgRecyclerView.visibility = View.VISIBLE
            textViewBg.visibility = View.GONE
        }

        val msgLayoutManager = LinearLayoutManager(this)
        msgAdapter = MsgAdapter(MsgListViewModel.msgList)
        msgRecyclerView.layoutManager = msgLayoutManager
        msgRecyclerView.adapter = msgAdapter

        val chatLayoutManager = LinearLayoutManager(this)
        chatLayoutManager.reverseLayout = true
        chatLayoutManager.stackFromEnd = true
        chatAdapter = ChatAdapter(this,chatListViewModel.chatList)
        chatRecyclerView.layoutManager = chatLayoutManager
        chatRecyclerView.adapter = chatAdapter

        topAppBar.title = chatViewModel.chatName
        chatViewModel.loadAllChats()

        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        topAppBar.setOnMenuItemClickListener {menuItem ->
            when (menuItem.itemId) {
                R.id.rename -> {
                    showRenameDialog()
                    true
                }
                R.id.delete -> {
                    deleteChatAndMsgs()
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
            sendMsg()
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
            addNewChat()
        }
        clearAllChatsBtn.setOnClickListener {
            clearAllChatsAndMsgs()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
               msgListViewModel.msgContentResult.collect { result ->
                   if (result != null && MsgListViewModel.msgList.size > 0) {
                       val msgContent = result.getOrNull()
                       if (msgContent != null) {
                           MsgListViewModel.msgList[MsgListViewModel.msgList.size - 1].content = msgContent
                           msgAdapter.notifyItemChanged(MsgListViewModel.msgList.size - 1)
                           val lastVisibleItemPosition = msgLayoutManager.findLastVisibleItemPosition()
                           val lastItem = msgLayoutManager.findViewByPosition(lastVisibleItemPosition)
                           val lastItemBottom = lastItem?.bottom ?: 0
                           msgRecyclerView.scrollBy(0, lastItemBottom)
                       } else {
                           Toasty.error(ChatGPTClientApplication.context, "请求失败", Toast.LENGTH_SHORT, true).show()
                           result.exceptionOrNull()?.printStackTrace()
                       }
                   }
               }
            }
        }

        msgListViewModel.isSend.observe(this) { isSend ->
            if (isSend) {
                sendMsg.visibility = View.VISIBLE
                loadingIndicator.visibility = View.GONE
            } else {
                sendMsg.visibility = View.GONE
                loadingIndicator.visibility = View.VISIBLE
            }
        }
        chatViewModel.chatsLiveData.observe(this) { result ->
            val chats = result.getOrNull()
            if (chats != null) {
                chatListViewModel.chatList.clear()
                chatListViewModel.chatList.addAll(chats)
                chatAdapter.notifyDataSetChanged()
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }
        chatViewModel.msgsLiveData.observe(this) { result ->
            val msgs = result.getOrNull()
            if ( msgs != null ) {
                if ((msgs.isNotEmpty() && msgs[0].content != "nonUpdateMsgList") || msgs.isEmpty()) {
                    MsgListViewModel.msgList.clear()
                    MsgListViewModel.msgList.addAll(msgs)
                    msgAdapter.notifyDataSetChanged()
                    if (MsgListViewModel.msgList.size > 0) {
                        msgRecyclerView.scrollToPosition(MsgListViewModel.msgList.size - 1)
                    }
                    drawerLayout.closeDrawers()
                }
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chatViewModel.nonUpdateMsgList()
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
        if (!chatViewModel.isChatGPT) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_rename,null)
            val editTextRename: EditText = dialogView.findViewById(R.id.et_rename)
            MaterialAlertDialogBuilder(this)
                .setTitle("重命名")
                .setView(dialogView)
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("确定") { dialog, _ ->
                    val text = editTextRename.text.toString()
                    chatViewModel.chatName = text
                    topAppBar.title = chatViewModel.chatName
                    chatViewModel.renameChatName(ChatViewModel.chatId!!,chatViewModel.chatName)
                    dialog.dismiss()
                }
                .show()
            editTextRename.setText(chatViewModel.chatName)
            editTextRename.requestFocus()
        }
    }

    /**
     * 删除对话及消息
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun deleteChatAndMsgs() {
        if (!chatViewModel.isChatGPT && msgListViewModel.isSend.value == true) {
            MaterialAlertDialogBuilder(this)
                .setTitle("删除该对话？")
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("确定") { dialog, _ ->
                    chatViewModel.deleteChat(ChatViewModel.chatId!!)
                    chatViewModel.deleteMsgsOfChat(ChatViewModel.chatId!!)
                    chatViewModel.chatName = "ChatGPT"
                    topAppBar.title = chatViewModel.chatName
                    msgRecyclerView.visibility = View.GONE
                    textViewBg.visibility = View.VISIBLE
                    chatViewModel.isChatGPT = true
                    ChatViewModel.chatId = null
                    dialog.dismiss()
                }
                .show()
        }
    }

    /**
     * 复制回复到剪切板
     */
    private fun copyResponse() {
        if (MsgListViewModel.msgList.size >= 2) {
            val msg = MsgListViewModel.msgList[MsgListViewModel.msgList.size-1]
            if (msg.type == Msg.TYPE_RECEIVED) {
                val clipboard =
                    ChatGPTClientApplication.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(null, msg.content))
                Toasty.success(ChatGPTClientApplication.context, "复制回复成功", Toast.LENGTH_SHORT, true).show()
            }
        }
    }

    /**
     * 清除所有对话
     */
    private fun clearAllChatsAndMsgs() {
        if (msgListViewModel.isSend.value == true) {
            MaterialAlertDialogBuilder(this)
                .setTitle("清除所有对话？")
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("确定") { dialog, _ ->
                    chatViewModel.clearAllChatsAndMsgs()
                    chatViewModel.chatName = "ChatGPT"
                    topAppBar.title = chatViewModel.chatName
                    msgRecyclerView.visibility = View.GONE
                    textViewBg.visibility = View.VISIBLE
                    chatViewModel.isChatGPT = true
                    ChatViewModel.chatId = null
                    dialog.dismiss()
                }
                .show()
        }
    }

    /**
     * 发送消息
     */
    private fun sendMsg() {
        val sendMsgStr = editTextMsg.text.toString()
        if (sendMsgStr.isNotEmpty() && msgListViewModel.isSend.value == true) {
            val msg = Msg(sendMsgStr, Msg.TYPE_SENT, ChatViewModel.chatId)
            if (ChatViewModel.chatId == null) {
                chatViewModel.isChatGPT = false
                chatViewModel.chatName = "New chat"
                topAppBar.title = chatViewModel.chatName
                msgRecyclerView.visibility = View.VISIBLE
                textViewBg.visibility = View.GONE
                chatViewModel.addNewChatAndMsg(sendMsgStr)
            } else {
                chatViewModel.addMsg(msg)
            }
            msgListViewModel.isSend.value = false
            MsgListViewModel.msgList.add(msg)
            msgAdapter.notifyItemInserted(MsgListViewModel.msgList.size -1 )
            msgRecyclerView.scrollToPosition(MsgListViewModel.msgList.size - 1)
            editTextMsg.text.clear()
            msgListViewModel.sendMessage(sendMsgStr)
        }
    }

    /**
     * 新增对话
     */
    private fun addNewChat() {
        if (msgListViewModel.isSend.value == true) {
            chatViewModel.isChatGPT = false
            chatViewModel.chatName = "New chat"
            topAppBar.title = chatViewModel.chatName
            msgRecyclerView.visibility = View.VISIBLE
            textViewBg.visibility = View.GONE
            chatViewModel.addNewChat()
            chatViewModel.addMsgsOfNewChat()
        }
    }

}