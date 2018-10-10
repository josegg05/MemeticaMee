package com.example.ragnarok.memeticamee.task

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.provider.ContactsContract
import com.example.ragnarok.memeticamee.model.AndroidContact
import java.lang.ref.WeakReference

/**
 * This task pushes into each possible contact's contact list (identified by email) myself as a possible contact.
 * For somebody to appear in my contact list, that somebody must first have executed this task (assuming
 * I appear in his contact list).
 */
class ContactSetupTask(val ref: WeakReference<Context>, val onComplete: (List<AndroidContact>) -> Unit) : AsyncTask<Unit, Unit, Unit>() {
     val contacts: MutableList<AndroidContact> = ArrayList()


    override fun doInBackground(vararg p0: Unit?) {
        val context = ref.get()
        if (context != null) {
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
        }
        return
    }

    override fun onPostExecute(result: Unit?) {
        onComplete(contacts)
    }
}