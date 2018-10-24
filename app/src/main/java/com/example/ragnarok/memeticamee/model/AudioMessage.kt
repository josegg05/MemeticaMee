package com.example.ragnarok.memeticamee.model

import java.util.*

data class AudioMessage(val audioPath: String,
                        val name: String,
                        override val time: Date,
                        override val senderId: String,
                        override val recipientId: String,
                        override val senderName: String,
                        override val type: String = MessageType.AUDIO)
    : Message {
    constructor() : this("","", Date(0), "", "", "", "")
}