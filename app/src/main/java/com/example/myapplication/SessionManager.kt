package com.example.myapplication

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
val USER_NAME_KEY = stringPreferencesKey("user_name")
val USER_EMAIL_KEY = stringPreferencesKey("user_email")

suspend fun salvarSessao(context: Context, response: AuthResponse) {
    context.dataStore.edit {
        it[ACCESS_TOKEN_KEY] = response.access_token
        it[USER_NAME_KEY] = response.user.name
        it[USER_EMAIL_KEY] = response.user.email
    }
}

suspend fun getAccessToken(context: Context): String? {
    return context.dataStore.data.first()[ACCESS_TOKEN_KEY]
}

suspend fun getUserName(context: Context): String? {
    return context.dataStore.data.first()[USER_NAME_KEY]
}

suspend fun getUserEmail(context: Context): String? {
    return context.dataStore.data.first()[USER_EMAIL_KEY]
}

suspend fun limparSessao(context: Context) {
    context.dataStore.edit {
        it.remove(ACCESS_TOKEN_KEY)
        it.remove(USER_NAME_KEY)
        it.remove(USER_EMAIL_KEY)
    }
}