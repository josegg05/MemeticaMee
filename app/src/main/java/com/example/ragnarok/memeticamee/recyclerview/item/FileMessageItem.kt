package com.example.ragnarok.memeticamee.recyclerview.item

import android.content.Context
import android.view.View
import com.example.ragnarok.memeticamee.R
import com.example.ragnarok.memeticamee.model.FileMessage
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.abc_activity_chooser_view.view.*
import kotlinx.android.synthetic.main.item_file_message.*

class FileMessageItem(val message: FileMessage,
                      val context: Context)
    : MessageItem(message) {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.textView_message_filetext.text = message.name + "." + message.extension + " size: " + message.size + "KB"
        viewHolder.textView_message_file_sender.visibility = View.GONE
        viewHolder.imageView_message_file.setImageResource(R.drawable.ic_insert_drive_file_black_24dp)
    }

    //cambiar
    override fun getLayout() = R.layout.item_file_message

    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if (other !is FileMessageItem)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as? FileMessageItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}