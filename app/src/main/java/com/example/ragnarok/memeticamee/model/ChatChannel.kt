package com.example.ragnarok.memeticamee.model

data class ChatChannel(val userIds: MutableList<String>) {
    constructor() : this(mutableListOf())
}