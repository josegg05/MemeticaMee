package com.example.ragnarok.memeticamee.fragment


import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.ragnarok.memeticamee.*

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
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast


class GroupFragment() : Fragment() {

    private lateinit var groupListenerRegistration: ListenerRegistration
    private lateinit var groupInvitedListenerRegistration: ListenerRegistration

    private var shouldInitRecyclerView = true
    private var shouldInitInvitedRecyclerView = true

    private lateinit var groupSection: Section
    private lateinit var groupInvitedSection: Section



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        //lista de usuarios registrados


        groupListenerRegistration =
                FirestoreUtil.addGroupListener2(this.activity!!, this::updateRecyclerView2)

        //TODO: Hacer un FirestoreUtil.addGroupInvitationListener
        groupInvitedListenerRegistration =
                FirestoreUtil.addIvitedGroupListener(this.activity!!, this::updateInvitedRecyclerView)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FirestoreUtil.removeListener(groupListenerRegistration)
        FirestoreUtil.removeListener(groupInvitedListenerRegistration)
        shouldInitRecyclerView = true
        shouldInitInvitedRecyclerView = true

    }

    private fun updateRecyclerView(itemsGroup: List<Item>, itemsInvited: List<Item>) {

        fun init() {
            recycler_view_groups.apply {
                layoutManager = LinearLayoutManager(this@GroupFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    groupSection = Section(itemsGroup)
                    add(groupSection)
                    setOnItemClickListener(onItemClick)
                }
            }

            recycler_view_groups_invitations.apply {
                layoutManager = LinearLayoutManager(this@GroupFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    groupInvitedSection = Section(itemsInvited)
                    add(groupInvitedSection)
                    setOnItemClickListener(onInvitedItemClick)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = groupSection.update(itemsGroup)

        fun updateInvitedItems() = groupInvitedSection.update(itemsInvited)

        if (shouldInitRecyclerView)
            init()
        else {
            updateItems()
            updateInvitedItems()
        }

    }

    private fun updateRecyclerView2(itemsGroup: List<Item>) {

        fun init() {
            recycler_view_groups.apply {
                layoutManager = LinearLayoutManager(this@GroupFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    groupSection = Section(itemsGroup)
                    add(groupSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitRecyclerView = false
        }

        fun updateItems() = groupSection.update(itemsGroup)


        if (shouldInitRecyclerView)
            init()
        else {
            updateItems()
        }

    }

    private fun updateInvitedRecyclerView(itemsInvited: List<Item>) {

        fun init() {
            recycler_view_groups_invitations.apply {
                layoutManager = LinearLayoutManager(this@GroupFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {
                    groupInvitedSection = Section(itemsInvited)
                    add(groupInvitedSection)
                    setOnItemClickListener(onInvitedItemClick)
                }
            }
            shouldInitInvitedRecyclerView = false
        }


        fun updateInvitedItems() = groupInvitedSection.update(itemsInvited)

        if (shouldInitInvitedRecyclerView)
            init()
        else {
            updateInvitedItems()
        }

    }

    private val onItemClick = OnItemClickListener { item, view ->

        if (item is GroupItem) {
            startActivity<GroupChatActivity>(
                    AppConstants.GROUP_NAME to item.group.name,
                    AppConstants.GROUP_ID to item.groupId
            )
        }

    }

    private val onInvitedItemClick = OnItemClickListener { item, view ->

        if (item is GroupItem) {
            val context = this@GroupFragment.context
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Invitación de grupo")

            val view = layoutInflater.inflate(R.layout.dialog_group_invitation, null)

            builder.setView(view);

            // set up the ok button
            builder.setPositiveButton("SI") { dialog, p1 ->
                val isValid = FirestoreUtil.acceptDeclineInvitation(false, item.groupId)

                if (isValid) {
                    // do something
                    toast("Miembro agregado")
                    dialog.dismiss()
                }
            }

            builder.setNegativeButton("NO") { dialog, p1 ->
                FirestoreUtil.acceptDeclineInvitation(true, item.groupId)
                dialog.dismiss()
            }

            builder.show()
        }
    }


}
