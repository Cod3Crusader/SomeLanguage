package com.archvin.token

sealed class Token {
    class SymbolToken(val value: String) : Token()
    data class TestToken(val raw: String) : Token()
}