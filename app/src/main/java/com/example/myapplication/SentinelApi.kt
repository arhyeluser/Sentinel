package com.example.myapplication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SentinelApi {

    private const val BASE_URL = "http://localhost:5000/api/analisar"

    data class RespostaBackend(
        val suspeito: Boolean,
        val confianca: Double,
        val motivos: List<String>
    )

    suspend fun analisar(
        app: String,
        texto: String,
        token: String? = null
    ): RespostaBackend? = withContext(Dispatchers.IO) {
        try {
            val url = URL(BASE_URL)
            val conexao = url.openConnection() as HttpURLConnection
            conexao.requestMethod = "POST"
            conexao.setRequestProperty("Content-Type", "application/json")
            if (!token.isNullOrBlank()) {
                conexao.setRequestProperty("Authorization", "Bearer $token")
            }
            conexao.doOutput = true
            conexao.connectTimeout = 8000
            conexao.readTimeout = 8000

            val corpo = JSONObject().apply {
                put("app", app)
                put("texto", texto)
            }

            conexao.outputStream.use { it.write(corpo.toString().toByteArray()) }

            if (conexao.responseCode == 200) {
                val resposta = conexao.inputStream.bufferedReader().readText()
                val json = JSONObject(resposta)
                val motivos = json.optJSONArray("motivos")
                val listaMotivos = mutableListOf<String>()
                if (motivos != null) {
                    for (i in 0 until motivos.length()) listaMotivos.add(motivos.getString(i))
                }
                RespostaBackend(
                    suspeito = json.optBoolean("suspeito", false),
                    confianca = json.optDouble("confianca", 0.0),
                    motivos = listaMotivos
                )
            } else {
                android.util.Log.w("SentinelApi", "Backend respondeu ${conexao.responseCode}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("SentinelApi", "Erro ao chamar backend: ${e.message}")
            null
        }
    }
}