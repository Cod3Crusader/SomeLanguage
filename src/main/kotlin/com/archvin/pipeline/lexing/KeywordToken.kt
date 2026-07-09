package com.archvin.pipeline.lexing

sealed class KeywordToken(raw: String) : Token(raw) {
    object ReturnKw : KeywordToken("return")
    object TrueKw : KeywordToken("true")
    object FalseKw : KeywordToken("false")
    object IfKw : KeywordToken("if")
    object ElseKw : KeywordToken("else")

    companion object {
        val map = mutableMapOf<String, KeywordToken>()

        private fun add(token: KeywordToken) {
            map[token.raw] = token
        }

        init {
            add(ReturnKw)
            add(TrueKw)
            add(FalseKw)
            add(IfKw)
            add(ElseKw)
        }
    }
}