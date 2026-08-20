package com.picu.app.data.local

import android.content.Context

/**
 * Ruolo del device salvato solo localmente (SharedPreferences).
 * Se i dati dell'app vengono cancellati, il device torna a chiedere "chi sei?".
 */
class LocalProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("picu_profile", Context.MODE_PRIVATE)

    var ruolo: String?
        get() = prefs.getString("ruolo", null)
        set(value) = prefs.edit().putString("ruolo", value).apply()

    var nome: String?
        get() = prefs.getString("nome", null)
        set(value) = prefs.edit().putString("nome", value).apply()

    fun isSetupComplete(): Boolean = ruolo != null
}
