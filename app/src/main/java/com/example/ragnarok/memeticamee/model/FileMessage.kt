package com.example.ragnarok.memeticamee.model

import java.util.*

data class FileMessage(val filePath: String,
                       val name: String,
                       val extension: String,
                       override val time: Date,
                       override val senderId: String,
                       override val recipientId: String,
                       override val senderName: String,
                       override val type: String = MessageType.FILE)
    : Message {
    constructor() : this("","", "", Date(0), "", "", "", "")
}