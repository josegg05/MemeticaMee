package com.example.ragnarok.memeticamee.model



/**
 * A contact stored on the device's contact list. Not to be confused with [MemeContact]
 * that represents validated AndroidContact on two devices. AndroidContacts can be promoted to MemeContacts after validation.
 */
class AndroidContact(val email: String, val name: String) {
    fun getUserContact() : User {
        return User(name, "", email, null, mutableListOf())
    }
}