package com.example.chatgptclient.ui.chat.chatmain

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.LeadingMarginSpan
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.chatgptclient.ChatGPTClientApplication
import com.example.chatgptclient.R
import com.example.chatgptclient.logic.model.Msg
import com.linhaodev.prism4jx.Prism4jGrammarLocator
import es.dmoral.toasty.Toasty
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.recycler.MarkwonAdapter
import io.noties.markwon.recycler.SimpleEntry
import io.noties.markwon.syntax.Prism4jSyntaxHighlight
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.utils.LeadingMarginUtils
import io.noties.prism4j.Prism4j
import org.commonmark.node.FencedCodeBlock

class MsgAdapter(private val msgList: List<Msg>, textView: TextView): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val markwon = Markwon.builder(ChatGPTClientApplication.context)
        .usePlugin(MyPlugin())
        .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
        .usePlugin(MarkwonInlineParserPlugin.create())
        .usePlugin(JLatexMathPlugin.create(textView.textSize,MyJLatexMathPlugin()))
        .build()

    inner class LeftViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val leftMsgRecyclerView: RecyclerView = view.findViewById(R.id.leftMsg)
    }

    inner class RightViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val rightMsg: TextView = view.findViewById(R.id.rightMsg)
    }

    override fun getItemViewType(position: Int): Int {
        val msg = msgList[position]
        return msg.type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: ViewHolder
        if (viewType == Msg.TYPE_RECEIVED) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.msg_left_item,parent,false)
            holder = LeftViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.msg_right_item,parent,false)
            holder = RightViewHolder(view)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = msgList[position]
        when (holder) {
            is LeftViewHolder -> {
                val markwonAapter: MarkwonAdapter = MarkwonAdapter.builderTextViewIsRoot(R.layout.adapter_default_entry)
                    .include(FencedCodeBlock::class.java, SimpleEntry.create(R.layout.adapter_fenced_code_block, R.id.text))
                    .build()
                val layoutManager = LinearLayoutManager(ChatGPTClientApplication.context)
                holder.leftMsgRecyclerView.layoutManager = layoutManager
                holder.leftMsgRecyclerView.adapter = markwonAapter
                markwonAapter.setMarkdown(markwon, Regex("(?<!\\$)\\$(?!\\$)").replace(msg.content){ matchResult -> "$$" })
            }
            is RightViewHolder -> holder.rightMsg.text = msg.content
            else -> throw IllegalAccessException()
        }
    }

    override fun getItemCount(): Int {
        return msgList.size
    }

    inner class MyPlugin : AbstractMarkwonPlugin() {

        override fun configureTheme(builder: MarkwonTheme.Builder) {
            builder.codeBlockTextColor(Color.WHITE)
            builder.codeBlockBackgroundColor(Color.BLACK)
        }

        override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
            val prism4j = Prism4j(Prism4jGrammarLocator())
            val highlight = Prism4jSyntaxHighlight.create(prism4j, Prism4jThemeDefault.create())
            builder.syntaxHighlight(highlight)
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
            builder.appendFactory(FencedCodeBlock::class.java) { _, _ ->
                CopyContentsSpan()
            }
            builder.appendFactory(FencedCodeBlock::class.java) { _, _ ->
                CopyIconSpan(getDrawable(ChatGPTClientApplication.context,R.drawable.ic_code_white_24dp)!!)
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
            Toasty.success(ChatGPTClientApplication.context, "复制成功", Toast.LENGTH_SHORT, true).show()
        }

        override fun updateDrawState(ds: TextPaint) {
            // do not apply link styling
        }
    }

    class CopyIconSpan(val icon: Drawable) : LeadingMarginSpan {

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

    inner class MyJLatexMathPlugin: JLatexMathPlugin.BuilderConfigure {
        override fun configureBuilder(builder: JLatexMathPlugin.Builder) {
            builder.let {
                it.inlinesEnabled(true)
                builder.theme().textColor(ContextCompat.getColor(ChatGPTClientApplication.context, R.color.royal_blue));
            }
        }
    }

}