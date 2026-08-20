package com.picu.app.data.model

data class Chat(
    val id: String = "",
    val tipo: String = "",
    val nome: String = "",
    val partecipanti: List<String> = emptyList()
)
