package com.archvin.token

sealed interface Token {
    data class TestToken(val raw: String) : Token // TODO: remove
    data class IdentifierToken(val id: String) : Token
    object NullToken : Token
}

