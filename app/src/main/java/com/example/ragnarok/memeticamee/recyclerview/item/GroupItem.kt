package com.example.ragnarok.memeticamee.recyclerview.item


import android.content.Context
import com.example.ragnarok.memeticamee.R
import com.example.ragnarok.memeticamee.glide.GlideApp
import com.example.ragnarok.memeticamee.model.Group
import com.example.ragnarok.memeticamee.util.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_group.*


class GroupItem(val group: Group,
                val groupId: String,
                private val context: Context)
    : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_group_name.text = group.name
        viewHolder.textView_group_bio.text = group.bio + ", creador:" + group.creator
        if (group.profilePicturePath != null)
            GlideApp.with(context)
                    .load(StorageUtil.pathToReference(group.profilePicturePath))
                    .placeholder(R.drawable.ic_people_black_24dp)
                    .into(viewHolder.imageView_profile_group_picture)
    }

    override fun getLayout() = R.layout.item_group
}