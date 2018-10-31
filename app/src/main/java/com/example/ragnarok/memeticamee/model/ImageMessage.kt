package com.example.ragnarok.memeticamee.model

import java.util.*

data class ImageMessage(val imagePath: String,
                        val size: String,
                        override val time: Date,
                        override val senderId: String,
                        override val recipientId: String,
                        override val senderName: String,
                        override val type: String = MessageType.IMAGE)
    : Message {
    constructor() : this("","", Date(0), "", "", "", "")
}