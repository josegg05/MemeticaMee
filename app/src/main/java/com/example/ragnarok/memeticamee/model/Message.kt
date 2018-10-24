package com.example.ragnarok.memeticamee.model

import java.util.*

object MessageType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
    const val FILE = "FILE"
    const val AUDIO = "AUDIO"
}

interface Message {
    val time: Date
    val senderId: String
    val recipientId: String
    val senderName: String
    val type: String
}