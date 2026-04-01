package com.archvin.token

import com.archvin.builtins.Chr
import com.archvin.builtins.Str
import com.archvin.type.HasType

sealed class LiteralToken<T>(open val value: T) : Token() {
    data class StringLiteral(override val value: String) : LiteralToken<String>(value), HasType {
        override val type = Str
    }

    data class CharLiteral(override val value: Char) : LiteralToken<Char>(value), HasType {
        override val type = Chr
    }

    sealed class NumberLiteral<T : Number>(override val value: T) : LiteralToken<T>(value), HasType {}
}