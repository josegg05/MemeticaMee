package com.example.ragnarok.memeticamee.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ragnarok.memeticamee.AppConstants
import com.example.ragnarok.memeticamee.ChatActivity
import com.example.ragnarok.memeticamee.GroupChatActivity

import com.example.ragnarok.memeticamee.R
import com.example.ragnarok.memeticamee.recyclerview.item.GroupItem
import com.example.ragnarok.memeticamee.util.FirestoreUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener

import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_group.*
import org.jetbrains.anko.support.v4.startActivity


class GroupFragment() : Fragment() {

    private lateinit var groupListenerRegistration: ListenerRegistration

    private var shouldInitRecyclerView = true

    private lateinit var groupSection: Section



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        //lista de usuarios registrados
        groupListenerRegistration =
                FirestoreUtil.addGroupListener(this.activity!!, this::updateRecyclerView)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FirestoreUtil.removeListener(groupListenerRegistration)
        shouldInitRecyclerView = true
    }

    private fun updateRecyclerView(items: List<Item>) {

        fun init() {
            recycler_view_groups.apply {
                layoutManager = LinearLayoutManager(this@GroupFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    groupSection = Section(items)
                    add(groupSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = groupSection.update(items)

        if (shouldInitRecyclerView)
            init()
        else
            updateItems()

    }

    private val onItemClick = OnItemClickListener { item, view ->

        if (item is GroupItem) {
            startActivity<GroupChatActivity>(
                    AppConstants.GROUP_NAME to item.group.name,
                    AppConstants.GROUP_ID to item.groupId
            )
        }

    }
}
