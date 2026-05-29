package com.archvin.pipeline.lexing

import com.archvin.data.Literal
import com.archvin.utils.Debug

sealed class Token(val raw: String) : Debug() {
    class Test(raw: String) : Token(raw) // TODO: remove
    class IdentifierToken(val id: String) : Token(id)
    class LiteralToken<out T>(val lit: Literal<T>) : Token(lit.toString())
}

