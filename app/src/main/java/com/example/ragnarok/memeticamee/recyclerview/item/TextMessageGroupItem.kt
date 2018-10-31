package com.example.ragnarok.memeticamee.recyclerview.item

import android.content.Context
import android.view.View
import com.example.ragnarok.memeticamee.R
import com.example.ragnarok.memeticamee.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_text_message.*

class TextMessageGroupItem(val message: TextMessage,
                           val context: Context)
    : MessageItem(message){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_message_text.text = message.text
        viewHolder.textView_message_sender.text = message.senderName
        super.bind(viewHolder, position)
    }

    override fun getLayout() = R.layout.item_text_message



    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if (other !is TextMessageGroupItem)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as? TextMessageGroupItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}