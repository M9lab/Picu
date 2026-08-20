package com.picu.app.ui.chatlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.picu.app.data.local.LocalProfileStore
import com.picu.app.data.model.Chat
import com.picu.app.data.repository.AuthRepository
import com.picu.app.data.repository.ChatRepository

@Composable
fun ChatListScreen(onOpenChat: (String) -> Unit) {
    val context = LocalContext.current
    val profileStore = remember { LocalProfileStore(context) }
    val authRepository = remember { AuthRepository() }
    val chatRepository = remember { ChatRepository() }

    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var caricamento by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = authRepository.currentUid ?: return@LaunchedEffect
        val lista = chatRepository.chatsForUser(uid)
        chats = lista.map { it.copy(nome = chatRepository.displayName(it, uid)) }
        caricamento = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Ciao ${profileStore.nome ?: ""}") })
        if (caricamento) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(chats) { chat ->
                    ListItem(
                        headlineContent = {
                            Text(chat.nome.ifBlank { chat.tipo }, style = MaterialTheme.typography.titleMedium)
                        },
                        modifier = Modifier.clickable { onOpenChat(chat.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
