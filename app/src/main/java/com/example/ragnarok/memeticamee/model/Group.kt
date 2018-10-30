package com.example.ragnarok.memeticamee.model

data class Group(val name: String,
                 val bio: String,
                 val creator: String,
                 val profilePicturePath: String?,
                 val members: MutableList<String>,
                 val registrationTokens: MutableList<String>) {
    constructor(): this("", "","", null, mutableListOf(), mutableListOf())
}