package com.archvin

import com.archvin.token.Reader
import com.archvin.token.Token
import com.archvin.token.Tokenizer

fun main(args: Array<String>) {
    val reader = Reader(args[0])

    Tokenizer(reader).tokenize().forEach { token ->
        val t = token as? Token.TestToken
        println( t?.raw ?: token ) }
}
