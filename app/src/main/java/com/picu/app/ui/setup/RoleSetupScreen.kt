package com.picu.app.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.picu.app.BuildConfig
import com.picu.app.data.local.LocalProfileStore
import com.picu.app.data.model.Role
import com.picu.app.data.repository.AuthRepository
import com.picu.app.data.repository.PresenceRepository
import kotlinx.coroutines.launch

@Composable
fun RoleSetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    val profileStore = remember { LocalProfileStore(context) }
    val authRepository = remember { AuthRepository() }
    val presenceRepository = remember { PresenceRepository() }
    val scope = rememberCoroutineScope()

    var selectedRole by remember { mutableStateOf<Role?>(null) }
    var nome by remember { mutableStateOf("") }
    var codice by remember { mutableStateOf("") }
    var errore by remember { mutableStateOf<String?>(null) }
    var caricando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Benvenuto su Picu", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        Text("Chi sei?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row {
            Role.entries.forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { selectedRole = role },
                    label = { Text(role.label) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Il tuo nome") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = codice,
            onValueChange = { codice = it },
            label = { Text("Codice famiglia") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        errore?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            enabled = selectedRole != null && nome.isNotBlank() && !caricando,
            onClick = {
                if (codice != BuildConfig.FAMILY_CODE) {
                    errore = "Codice famiglia non corretto"
                    return@Button
                }
                val role = selectedRole ?: return@Button
                caricando = true
                errore = null
                scope.launch {
                    try {
                        val uid = authRepository.signInAnonymouslyIfNeeded()
                        authRepository.saveProfile(uid, role.name, nome.trim())
                        authRepository.refreshFcmToken(uid)
                        presenceRepository.startPresence(uid)
                        profileStore.ruolo = role.name
                        profileStore.nome = nome.trim()
                        onSetupComplete()
                    } catch (e: Exception) {
                        errore = "Errore di connessione, riprova"
                    } finally {
                        caricando = false
                    }
                }
            }
        ) {
            Text(if (caricando) "Attendere..." else "Entra")
        }
    }
}
