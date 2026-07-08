package com.archvin.pipeline.lexing

sealed class KeywordToken(raw: String) : Token(raw) {
    object ReturnKw : KeywordToken("return")

    companion object {
        val map = mutableMapOf<String, KeywordToken>()

        private fun add(token: KeywordToken) {
            map[token.raw] = token
        }

        init {
            add(ReturnKw)
        }
    }
}