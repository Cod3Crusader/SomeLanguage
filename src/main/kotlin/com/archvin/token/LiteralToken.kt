package com.archvin.token

import com.archvin.type.BuiltinType
import com.archvin.type.HasType

sealed class LiteralToken<out T>(val value: T) : Token(value.toString()), HasType {
    abstract override val type: BuiltinType<T>

    class StringLiteral(value: String) : LiteralToken<String>(value) {
        override val type = BuiltinType.StrType
    }

    class CharLiteral(value: Char) : LiteralToken<Char>(value) {
        override val type = BuiltinType.CharType
    }

    sealed class NumberLiteral<T : Number>(value: T) : LiteralToken<T>(value) {
        class I32Literal(value: Int) : NumberLiteral<Int>(value) {
            override val type = BuiltinType.I32Type
            override val className = "I32Lit"
        }
    }
}