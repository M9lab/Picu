package com.picu.app.data.model

data class Message(
    val id: String = "",
    val mittente: String = "",
    val testo: String = "",
    val timestamp: Long = 0L,
    val letto: Boolean = false
)
