package com.picu.app.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.picu.app.data.model.Chat
import com.picu.app.data.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun chatsForUser(uid: String): List<Chat> {
        val snapshot = firestore.collection("chats")
            .whereArrayContains("partecipanti", uid)
            .get()
            .await()
        return snapshot.documents.map { it.toChat() }
    }

    suspend fun chat(chatId: String): Chat? {
        val doc = firestore.collection("chats").document(chatId).get().await()
        if (!doc.exists()) return null
        return doc.toChat()
    }

    suspend fun userName(uid: String): String {
        val doc = firestore.collection("users").document(uid).get().await()
        return doc.getString("nome") ?: "?"
    }

    /** Nome da mostrare per una chat: gli altri partecipanti, esclusi io. */
    suspend fun displayName(chat: Chat, myUid: String): String {
        val altri = chat.partecipanti.filter { it != myUid }
        if (altri.isEmpty()) return chat.tipo
        return altri.joinToString(" e ") { userName(it) }
    }

    fun messagesFlow(chatId: String): Flow<List<Message>> = callbackFlow {
        val registration = firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.map { doc ->
                    Message(
                        id = doc.id,
                        mittente = doc.getString("mittente") ?: "",
                        testo = doc.getString("testo") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        letto = doc.getBoolean("letto") ?: false
                    )
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    suspend fun sendMessage(chatId: String, mittente: String, testo: String) {
        val message = mapOf(
            "mittente" to mittente,
            "testo" to testo,
            "timestamp" to System.currentTimeMillis(),
            "letto" to false
        )
        firestore.collection("chats").document(chatId)
            .collection("messages")
            .add(message)
            .await()
    }

    private fun DocumentSnapshot.toChat(): Chat = Chat(
        id = id,
        tipo = getString("tipo") ?: "",
        partecipanti = get("partecipanti") as? List<String> ?: emptyList()
    )
}
