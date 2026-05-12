package com.archvin.token

sealed interface Token {
    data class Test(val raw: String) : Token // TODO: remove
    data class Identifier(val id: String) : Token
    object NullToken : Token
}

