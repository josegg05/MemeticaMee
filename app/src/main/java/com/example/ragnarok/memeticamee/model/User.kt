package com.example.ragnarok.memeticamee.model

data class User(val name: String,
                val bio: String,
                val email: String,
                val profilePicturePath: String?,
                val registrationTokens: MutableList<String>) {
    constructor(): this("", "","", null, mutableListOf())
}