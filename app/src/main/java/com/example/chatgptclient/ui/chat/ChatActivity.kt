package com.example.chatgptclient.ui.chat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Layout
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.LeadingMarginSpan
import android.text.util.Linkify
import android.util.Log
import android.view.MotionEvent
import android.view.View
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
import com.example.chatgptclient.ChatGPTClientApplication
import com.example.chatgptclient.R
import com.example.chatgptclient.ui.chat.chatmain.ChatMainViewModel
import com.example.chatgptclient.utils.Prism4jGrammarLocator
import com.google.android.material.appbar.MaterialToolbar
import io.noties.markwon.*
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.recycler.MarkwonAdapter
import io.noties.markwon.recycler.SimpleEntry
import io.noties.markwon.syntax.Prism4jSyntaxHighlight
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.utils.LeadingMarginUtils
import io.noties.prism4j.Prism4j
import io.noties.prism4j.Prism4j.*
import org.commonmark.node.FencedCodeBlock


class ChatActivity : AppCompatActivity() {
    private lateinit var topAppBar: MaterialToolbar

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var editTextMsg: EditText

    private lateinit var sendMsg: Button

    private lateinit var textViewMsg: TextView

    private lateinit var markwon: Markwon

    private lateinit var adapter: MarkwonAdapter

    private lateinit var recyclerView: RecyclerView

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
//        textViewMsg = findViewById(R.id.tvMsg)
        adapter = MarkwonAdapter.builderTextViewIsRoot(R.layout.adapter_default_entry)
            .include(FencedCodeBlock::class.java, SimpleEntry.create(R.layout.adapter_fenced_code_block, R.id.text))
            .build()
        recyclerView = findViewById(R.id.rv_msg)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        markwon = Markwon.builder(this)
            .usePlugin(MyPlugin())
            .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
            .build()

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
                editTextMsg.text.clear()
                chatMainViewModel.sendMessage(sendMsgStr)
            }
        }

        chatMainViewModel.chatCompletionLiveData.observe(this, Observer { result ->
            val chatCompletion = result.getOrNull()
            if (chatCompletion != null) {
                chatCompletion.choices[0].message?.let {
                    Log.d("linhao",it.content)
                    adapter.setMarkdown(markwon,it.content)
                    adapter.notifyDataSetChanged()
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

    inner class MyPlugin : AbstractMarkwonPlugin() {

        override fun configureTheme(builder: MarkwonTheme.Builder) {
            builder.codeBlockTextColor(Color.WHITE)
            builder.codeBlockBackgroundColor(Color.BLACK)
        }

        override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
            val prism4j = Prism4j(Prism4jGrammarLocator())
            val highlight = Prism4jSyntaxHighlight.create(prism4j,Prism4jThemeDefault.create())
            builder.syntaxHighlight(highlight)
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
            builder.appendFactory(FencedCodeBlock::class.java) { _, _ ->
                CopyContentsSpan()
            }
            builder.appendFactory(FencedCodeBlock::class.java) { _, _ ->
                CopyIconSpan(getDrawable(R.drawable.ic_code_white_24dp)!!)
            }
        }
    }

    class CopyContentsSpan() : ClickableSpan() {
        override fun onClick(widget: View) {
            val spanned = (widget as? TextView)?.text as? Spanned ?: return
            val start = spanned.getSpanStart(this)
            val end = spanned.getSpanEnd(this)
            // by default code blocks have new lines before and after content
            val contents = spanned.subSequence(start, end).toString().trim()

            val clipboard = ChatGPTClientApplication.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(null,contents))
            Toast.makeText(ChatGPTClientApplication.context,"复制成功",Toast.LENGTH_SHORT).show()
        }

        override fun updateDrawState(ds: TextPaint) {
            // do not apply link styling
        }
    }

    class CopyIconSpan(val icon: Drawable) : LeadingMarginSpan{

        init {
            if (icon.bounds.isEmpty) {
                icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
            }
        }

        override fun getLeadingMargin(first: Boolean): Int = 0

        override fun drawLeadingMargin(
            c: Canvas,
            p: Paint,
            x: Int,
            dir: Int,
            top: Int,
            baseline: Int,
            bottom: Int,
            text: CharSequence,
            start: Int,
            end: Int,
            first: Boolean,
            layout: Layout
        ) {

            // called for each line of text, we are interested only in first one
            if (!LeadingMarginUtils.selfStart(start, text, this)) return

            val save = c.save()
            try {
                // horizontal position for icon
                val w = icon.bounds.width().toFloat()
                // minus quarter width as padding
                val left = layout.width - w - (w / 4F)
                c.translate(left, top.toFloat())
                icon.draw(c)
            } finally {
                c.restoreToCount(save)
            }
        }
    }

}