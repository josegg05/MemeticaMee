package com.example.ragnarok.memeticamee.recyclerview.item

import android.content.Context
import com.example.ragnarok.memeticamee.R
import com.example.ragnarok.memeticamee.glide.GlideApp
import com.example.ragnarok.memeticamee.model.AudioMessage
import com.example.ragnarok.memeticamee.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_audio_message.*


class AudioMessageItem(val message: AudioMessage,
                       val context: Context)
    : MessageItem(message) {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        viewHolder.textView_message_audiotext.text = message.name
    }

    //cambiar
    override fun getLayout() = R.layout.item_audio_message

    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if (other !is AudioMessageItem)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as? AudioMessageItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}