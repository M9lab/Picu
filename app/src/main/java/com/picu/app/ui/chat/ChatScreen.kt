package com.picu.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.picu.app.data.repository.AuthRepository
import com.picu.app.data.repository.ChatRepository
import com.picu.app.data.repository.PresenceRepository
import com.picu.app.data.model.Message
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(chatId: String, onBack: () -> Unit) {
    val authRepository = remember { AuthRepository() }
    val chatRepository = remember { ChatRepository() }
    val presenceRepository = remember { PresenceRepository() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var testo by remember { mutableStateOf("") }
    var titolo by remember { mutableStateOf("") }
    var altriUid by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var statoOnline by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    LaunchedEffect(chatId) {
        val uid = authRepository.currentUid ?: return@LaunchedEffect
        val chat = chatRepository.chat(chatId) ?: return@LaunchedEffect
        val altri = chat.partecipanti.filter { it != uid }
        val risolti = altri.map { it to chatRepository.userName(it) }
        altriUid = risolti
        titolo = risolti.joinToString(" e ") { it.second }
        altri.forEach { altroUid ->
            launch {
                presenceRepository.observeStatus(altroUid).collect { online ->
                    statoOnline = statoOnline + (altroUid to online)
                }
            }
        }
    }

    LaunchedEffect(chatId) {
        chatRepository.messagesFlow(chatId).collect { lista ->
            messages = lista
            if (lista.isNotEmpty()) listState.animateScrollToItem(lista.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(titolo, style = MaterialTheme.typography.titleMedium)
                    val sottotitolo = altriUid.joinToString(" · ") { (uid, nome) ->
                        val online = statoOnline[uid] == true
                        "$nome ${if (online) "🟢 online" else "⚪ offline"}"
                    }
                    if (sottotitolo.isNotBlank()) {
                        Text(sottotitolo, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            state = listState
        ) {
            items(messages) { message ->
                val mio = message.mittente == authRepository.currentUid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (mio) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (mio) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            message.testo,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = testo,
                onValueChange = { testo = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Scrivi un messaggio...") }
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                val uid = authRepository.currentUid ?: return@IconButton
                val contenuto = testo.trim()
                if (contenuto.isEmpty()) return@IconButton
                testo = ""
                scope.launch { chatRepository.sendMessage(chatId, uid, contenuto) }
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Invia")
            }
        }
    }
}
