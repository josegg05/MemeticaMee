package com.example.ragnarok.memeticamee.util

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.support.v4.content.ContextCompat


import android.util.Log
import com.example.ragnarok.memeticamee.model.*
import com.example.ragnarok.memeticamee.recyclerview.item.*
import com.example.ragnarok.memeticamee.task.ContactSetupTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item
import java.lang.NullPointerException
import java.lang.ref.WeakReference


object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy {FirebaseFirestore.getInstance()}

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

    private val currentGroupDocRef: DocumentReference
        get() = firestoreInstance.document("groups/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

    //private lateinit var localContacts: List<AndroidContact>

    private val chatChannelsCollectionRef = firestoreInstance.collection("chatChannels")

    private val groupChannelsCollectionRef = firestoreInstance.collection("groupChannels")


    fun initCurrentUserIfFirstTime(onComplete: () -> Unit){
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()){
                val newUser = User(FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                        "", FirebaseAuth.getInstance().currentUser?.email ?: "", null, mutableListOf())
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }
            else {
                onComplete()
            }
        }
    }


    fun updateCurrentUser(name: String = "", bio: String = "", profilePicturePath: String? = null){
        val userFieldMap = mutableMapOf<String,Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (bio.isNotBlank()) userFieldMap["bio"] = bio
        if (profilePicturePath != null)
            userFieldMap["profilePicturePath"] = profilePicturePath
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit){
        currentUserDocRef.get()
                .addOnSuccessListener {
                    onComplete(it.toObject(User::class.java)!!)
                }
    }


    fun createGroup(name: String = "", bio: String = "", profilePicturePath: String? = null){

        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val groupToCreate = Group(name, bio, currentUserId, profilePicturePath, mutableListOf(),mutableListOf())

        val newChannel = groupChannelsCollectionRef.document()
        newChannel.set(groupToCreate)

        currentUserDocRef
                .collection("engagedGroupChannels")
                .document(newChannel.id)
                .set(mapOf("channelId" to newChannel.id))
    }


    //Función que extrae los usuarios registrados y se rearga cada vez que e agrega uno nuevo
    fun addUsersListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("users")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    //exp1i

                    var permissionCheck = ContextCompat.checkSelfPermission(context,
                            android.Manifest.permission.READ_CONTACTS);

                    if (permissionCheck == 0) {
                        val contacts: MutableList<AndroidContact> = ArrayList()
                        val contactCursor = context.contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME)
                        while (contactCursor.moveToNext()) {
                            val contactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID))
                            val contactName = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                            var contactEmail: String? = null

                            val emailCursor = context.contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                                    "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = $contactId", null, null)
                            if (emailCursor.moveToFirst()) {
                                // TODO: a contact can have multiple emails, for this demo, we are only considering the first one he may have registered
                                contactEmail = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                            }
                            emailCursor.close()

                            if (contactEmail != null) {
                                contacts.add(AndroidContact(contactEmail, contactName))
                            }
                        }
                        contactCursor.close()
                        //exp1f


                        var isContact = false
                        val items = mutableListOf<Item>()
                        querySnapshot!!.documents.forEach { doc1 ->
                            if (doc1.id != FirebaseAuth.getInstance().currentUser?.uid) {
                                contacts.forEach {
                                    if (doc1["email"] == it.email)
                                        isContact = true
                                }
                                if (isContact) {
                                    items.add(PersonItem(doc1.toObject(User::class.java)!!, doc1.id, context))
                                }
                                isContact = false
                            }
                        }
                        onListen(items)
                    }
                }
    }


    fun addGroupListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("groupChannels")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "Group listener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach { doc1 ->
                        val groupX = currentUserDocRef.collection("engagedGroupChannels")
                                .document(doc1.id).get()
                                    if (groupX != null) {
                                        //TODO: puede que haya que cambiar en "GroupItem" el userID por groupID. Si no, hay que cambiar acá abajo el "doc1.id" por el userID
                                        items.add(GroupItem(doc1.toObject(Group::class.java)!!, doc1.id, context))
                                    }
                    }
                    onListen(items)
                }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun getOrCreateChatChannel(otherUserId: String,
    onComplete: (channelId: String) -> Unit) {
        currentUserDocRef.collection("engagedChatChannels")
                .document(otherUserId).get().addOnSuccessListener {
                    if (it.exists()) {
                        onComplete(it["channelId"] as String)
                        return@addOnSuccessListener
                    }

                    val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                    val newChannel = chatChannelsCollectionRef.document()
                    newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                    currentUserDocRef
                            .collection("engagedChatChannels")
                            .document(otherUserId)
                            .set(mapOf("channelId" to newChannel.id))

                    firestoreInstance.collection("users").document(otherUserId)
                            .collection("engagedChatChannels")
                            .document(currentUserId)
                            .set(mapOf("channelId" to newChannel.id))

                    onComplete(newChannel.id)
                }
    }

    fun addChatMessagesListener(channelId: String, context: Context,
                                onListen: (List<Item>) -> Unit): ListenerRegistration {
        return chatChannelsCollectionRef.document(channelId).collection("messages")
                .orderBy("time")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "ChatMessagesListener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach {
                        if (it["type"] == MessageType.TEXT)
                            items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!, context))
                        else if (it["type"] == MessageType.IMAGE)
                            items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!, context))
                        else if (it["type"] == MessageType.FILE)
                            items.add(FileMessageItem(it.toObject(FileMessage::class.java)!!, context))
                        else if (it["type"] == MessageType.AUDIO)
                            items.add(AudioMessageItem(it.toObject(AudioMessage::class.java)!!, context))
                        return@forEach
                    }
                    onListen(items)
                }
    }

    fun sendMessage(message: Message, channelId: String) {
        chatChannelsCollectionRef.document(channelId)
                .collection("messages")
                .add(message)
    }

    //TODO: Arreglar estas dos funciones de abajo para el chat de grupo
    fun addGroupChatMessagesListener(channelId: String, context: Context,
                                onListen: (List<Item>) -> Unit): ListenerRegistration {
        return groupChannelsCollectionRef.document(channelId).collection("messages")
                .orderBy("time")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "ChatMessagesListener error.", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    querySnapshot!!.documents.forEach {
                        if (it["type"] == MessageType.TEXT)
                            items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!, context))
                        else if (it["type"] == MessageType.IMAGE)
                            items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!, context))
                        else if (it["type"] == MessageType.FILE)
                            items.add(FileMessageItem(it.toObject(FileMessage::class.java)!!, context))
                        else if (it["type"] == MessageType.AUDIO)
                            items.add(AudioMessageItem(it.toObject(AudioMessage::class.java)!!, context))
                        return@forEach
                    }
                    onListen(items)
                }
    }

    fun sendGroupMessage(message: Message, channelId: String) {
        groupChannelsCollectionRef.document(channelId)
                .collection("messages")
                .add(message)
    }

    //region FCM
    fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject(User::class.java)!!
            onComplete(user.registrationTokens)
        }
    }

    fun setFCMRegistrationTokens(registrationTokens: MutableList<String>) {
        currentUserDocRef.update(mapOf("registrationTokens" to registrationTokens))
    }
    //endregion FCM
}