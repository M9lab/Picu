package com.picu.app.data.model

data class User(
    val uid: String = "",
    val ruolo: String = "",
    val nome: String = "",
    val fcmToken: String? = null
)
