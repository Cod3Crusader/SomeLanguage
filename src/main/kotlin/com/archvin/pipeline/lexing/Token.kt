package com.archvin.pipeline.lexing

import com.archvin.utils.Debug
import com.archvin.variable.Literal

sealed class Token(val raw: String) : Debug() {
    class Test(raw: String) : Token(raw) // TODO: remove
    class IdentifierToken(val id: String) : Token(id)
    class LiteralToken<out T>(val lit: Literal<T>) : Token(lit.toString())
}

