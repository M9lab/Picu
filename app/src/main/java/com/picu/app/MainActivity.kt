package com.picu.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.picu.app.data.local.LocalProfileStore
import com.picu.app.data.repository.AuthRepository
import com.picu.app.data.repository.PresenceRepository
import com.picu.app.ui.navigation.PicuNavGraph
import com.picu.app.ui.navigation.Routes
import com.picu.app.ui.theme.PicuTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val profileStore = LocalProfileStore(this)
        val start = if (profileStore.isSetupComplete()) Routes.CHAT_LIST else Routes.SETUP

        // Se il device ha già un ruolo salvato, riaggancia sessione, token FCM
        // e presenza online senza dover rifare il setup.
        if (profileStore.isSetupComplete()) {
            val authRepository = AuthRepository()
            val presenceRepository = PresenceRepository()
            lifecycleScope.launch {
                val uid = authRepository.signInAnonymouslyIfNeeded()
                authRepository.refreshFcmToken(uid)
                presenceRepository.startPresence(uid)
            }
        }

        setContent {
            PicuTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PicuNavGraph(startDestination = start)
                }
            }
        }
    }
}
