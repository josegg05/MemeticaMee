package com.example.ragnarok.memeticamee.util

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.ragnarok.memeticamee.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.*

object StorageUtil {
    public val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    private val currentUserRef: StorageReference
        get() = storageInstance.reference
                .child(FirebaseAuth.getInstance().currentUser?.uid
                        ?: throw NullPointerException("UID is null."))

    fun uploadProfilePhoto(imageBytes: ByteArray,
                           onSuccess: (imagePath: String) -> Unit) {
        val ref = currentUserRef.child("profilePictures/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes)
                .addOnSuccessListener {
                    onSuccess(ref.path)
                }
    }

    fun uploadGroupPhoto(imageBytes: ByteArray,
                           onSuccess: (imagePath: String) -> Unit) {
        val ref = currentUserRef.child("GroupProfilePictures/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes)
                .addOnSuccessListener {
                    onSuccess(ref.path)
                }
    }

    fun uploadMessageImage(imageBytes: ByteArray,
                           context: Context,
                           onSuccess: (imagePath: String) -> Unit) {
        val ref = currentUserRef.child("messages/${UUID.nameUUIDFromBytes(imageBytes)}")
        try {
        ref.putBytes(imageBytes)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }.addOnProgressListener { taskSnapshot ->
                Toast.makeText(context, "Subiendo", Toast.LENGTH_SHORT).show()
            }
        }catch (e: Exception) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    /*fun uploadMessageFile(uri: Uri,
                          context: Context,
                           onSuccess: (filePath: String) -> Unit) {
        var mReference = currentUserRef.child("mesagges/" + uri.lastPathSegment)
        try {
            mReference.putFile(uri).addOnSuccessListener {
                taskSnapshot: UploadTask.TaskSnapshot? -> var url = taskSnapshot!!.downloadUrl.toString()

            }.addOnProgressListener { taskSnapshot ->
                Toast.makeText(context, "Subiendo", Toast.LENGTH_LONG).show()
            }
        }catch (e: Exception) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
        }
    }*/

    fun uploadMessageAudio(audioBytes: ByteArray,
                           onSuccess: (audioPath: String) -> Unit) {
        val ref = currentUserRef.child("messages/${UUID.nameUUIDFromBytes(audioBytes)}")
        ref.putBytes(audioBytes)
                .addOnSuccessListener {
                    onSuccess(ref.path)
                }
    }

    fun pathToReference(path: String) = storageInstance.getReference(path)
}