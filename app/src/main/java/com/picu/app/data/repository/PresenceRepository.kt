package com.picu.app.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Presenza online/offline via Firebase Realtime Database.
 * Usa il meccanismo nativo `.info/connected` + `onDisconnect`, l'unico modo
 * affidabile per sapere quando un device si disconnette (es. il bambino
 * esce dal raggio del Wi-Fi di casa).
 */
class PresenceRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    fun startPresence(uid: String) {
        val statusRef = database.getReference("status/$uid")
        val connectedRef = database.getReference(".info/connected")

        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    statusRef.onDisconnect().setValue(
                        mapOf("online" to false, "lastSeen" to System.currentTimeMillis())
                    )
                    statusRef.setValue(mapOf("online" to true, "lastSeen" to System.currentTimeMillis()))
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun observeStatus(uid: String): Flow<Boolean> = callbackFlow {
        val ref = database.getReference("status/$uid/online")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) ?: false)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
