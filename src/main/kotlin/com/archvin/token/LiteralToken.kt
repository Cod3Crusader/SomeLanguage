package com.archvin.token

import com.archvin.type.CharType
import com.archvin.type.I32Type
import com.archvin.type.StrType
import com.archvin.type.HasType

sealed interface LiteralToken<out T> : Token, HasType {
    val value: T

    data class StringLiteral(override val value: String) : LiteralToken<String> {
        override val type = StrType
    }

    data class CharLiteral(override val value: Char) : LiteralToken<Char> {
        override val type = CharType
    }

    sealed class NumberLiteral<T : Number>(override val value: T) : LiteralToken<T> {
        data class I32Literal(override val value: Int) : LiteralToken.NumberLiteral<Int>(value) {
            override val type = I32Type
        }
    }
}