package com.picu.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUid: String? get() = auth.currentUser?.uid

    suspend fun signInAnonymouslyIfNeeded(): String {
        auth.currentUser?.let { return it.uid }
        val result = auth.signInAnonymously().await()
        return result.user!!.uid
    }

    suspend fun saveProfile(uid: String, ruolo: String, nome: String) {
        firestore.collection("users").document(uid)
            .set(mapOf("ruolo" to ruolo, "nome" to nome))
            .await()
    }

    suspend fun refreshFcmToken(uid: String) {
        val token = FirebaseMessaging.getInstance().token.await()
        firestore.collection("users").document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
            .await()
    }
}
