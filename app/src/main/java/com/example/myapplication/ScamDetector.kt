package com.example.myapplication

import java.text.Normalizer

enum class NivelRisco { SEGURO, DUVIDOSO, SUSPEITO }

object ScamDetector {

    private data class Categoria(val nome: String, val palavras: List<String>, val peso: Int)

    private val categorias = listOf(
        Categoria("Pedido de dinheiro/Pix", listOf("pix", "transferencia", "transferir", "boleto", "cartao", "deposito", "depositar", "dinheiro"), 20),
        Categoria("Ameaça/coerção", listOf("bloquear", "bloqueio", "bloqueada", "cancelar", "cancelado", "suspender", "suspenso", "multa", "perder", "perda", "negativado", "negativar"), 25),
        Categoria("Urgência", listOf("urgente", "urgencia", "agora", "imediato", "imediatamente", "hoje", "rapido", "ja", "ultima chance", "nao desligue", "fique na linha"), 15),
        Categoria("Pedido de dados sensíveis", listOf("senha", "codigo", "cpf", "confirme", "confirmar", "digite", "informe", "cartao de credito", "dados bancarios"), 25),
        Categoria("Isca de prêmio", listOf("premio", "ganhou", "sorteio", "sorteado", "parabens"), 20),
        Categoria("Link suspeito", listOf("clique", "acesse o link", "clique aqui", "clique no link"), 20)
    )

    private const val LIMIAR_NOTIFICAR = 50
    private const val LIMIAR_IA = 65

    data class Resultado(
        val nivel: NivelRisco,
        val score: Int,
        val motivos: List<String>
    )

    private fun normalizar(texto: String): String {
        val semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return semAcento.lowercase()
    }

    fun analisar(texto: String?, sinaisExtras: List<Pair<String, Int>> = emptyList()): Resultado {
        val textoNormalizado = texto?.let { normalizar(it) } ?: ""

        val categoriasEncontradas = categorias.filter { categoria ->
            categoria.palavras.any { palavra -> textoNormalizado.contains(palavra) }
        }

        val scoreTexto = categoriasEncontradas.sumOf { it.peso }
        val motivosTexto = categoriasEncontradas.map { it.nome }

        val motivosExtras = sinaisExtras.map { it.first }
        val scoreExtras = sinaisExtras.sumOf { it.second }

        val scoreTotal = scoreTexto + scoreExtras
        val motivosFinais = motivosExtras + motivosTexto

        if (texto.isNullOrBlank() && sinaisExtras.isEmpty()) {
            return Resultado(NivelRisco.SEGURO, 0, emptyList())
        }

        val nivel = when {
            scoreTotal >= LIMIAR_IA -> NivelRisco.SUSPEITO
            scoreTotal >= LIMIAR_NOTIFICAR -> NivelRisco.DUVIDOSO
            else -> NivelRisco.SEGURO
        }

        return Resultado(nivel, scoreTotal, motivosFinais)
    }
}