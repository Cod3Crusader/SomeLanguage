package com.archvin.token

import com.archvin.type.BuiltinType
import com.archvin.type.HasType

sealed interface LiteralToken<out T> : Token, HasType {
    val value: T

    data class StringLiteral(override val value: String) : LiteralToken<String> {
        override val type = BuiltinType.StrType
    }

    data class CharLiteral(override val value: Char) : LiteralToken<Char> {
        override val type = BuiltinType.CharType
    }

    sealed class NumberLiteral<T : Number>(override val value: T) : LiteralToken<T> {
        data class I32Literal(override val value: Int) : NumberLiteral<Int>(value) {
            override val type = BuiltinType.I32Type
        }
    }
}