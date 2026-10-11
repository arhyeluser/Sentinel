package com.example.myapplication

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class StatusAlerta { CONFIRMADO_LOCAL, ANALISANDO, CONFIRMADO_IA, DESCARTADO, OFICIAL }

data class Alerta(
    val id: String = UUID.randomUUID().toString(),
    val app: String,
    val trecho: String,
    var motivos: List<String>,
    val horario: String,
    var status: StatusAlerta
)

object SentinelState {
    val alertas = mutableStateListOf<Alerta>()

    var totalAnalisadas by mutableStateOf(0)
        private set

    var golpesEvitados by mutableStateOf(0)
        private set

    private var appContext: Context? = null
    private const val PREFS = "sentinel_contadores"

    fun inicializar(context: Context) {
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        totalAnalisadas = prefs.getInt("total_analisadas", 0)
        golpesEvitados = prefs.getInt("golpes_evitados", 0)
    }

    fun registrarAnalise() {
        totalAnalisadas += 1
        salvar()
    }

    fun registrar(app: String, trecho: String, motivos: List<String>, status: StatusAlerta): String {
        val formato = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val alerta = Alerta(
            app = app,
            trecho = trecho.take(150),
            motivos = motivos,
            horario = formato.format(Date()),
            status = status
        )
        alertas.add(0, alerta)
        if (alertas.size > 50) alertas.removeAt(alertas.size - 1)

        if (status == StatusAlerta.CONFIRMADO_LOCAL || status == StatusAlerta.CONFIRMADO_IA) {
            golpesEvitados += 1
        }
        salvar()

        return alerta.id
    }

    fun atualizar(id: String, suspeito: Boolean, motivos: List<String>) {
        val index = alertas.indexOfFirst { it.id == id }
        if (index == -1) return

        val statusAnterior = alertas[index].status
        val novoStatus = if (suspeito) StatusAlerta.CONFIRMADO_IA else StatusAlerta.DESCARTADO

        alertas[index] = alertas[index].copy(status = novoStatus, motivos = motivos)

        if (suspeito && statusAnterior != StatusAlerta.CONFIRMADO_LOCAL) {
            golpesEvitados += 1
            salvar()
        }
    }

    private fun salvar() {
        val prefs = appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE) ?: return
        prefs.edit {
            putInt("total_analisadas", totalAnalisadas)
            putInt("golpes_evitados", golpesEvitados)
        }
    }
}