package com.archvin.token

import com.archvin.type.BuiltinType
import com.archvin.type.HasType

sealed class LiteralToken<out T> : Token(), HasType {
    abstract val value: T

    class StringLiteral(override val value: String) : LiteralToken<String>() {
        override val type = BuiltinType.StrType
    }

    class CharLiteral(override val value: Char) : LiteralToken<Char>() {
        override val type = BuiltinType.CharType
    }

    sealed class NumberLiteral<T : Number>(override val value: T) : LiteralToken<T>() {
        class I32Literal(override val value: Int) : NumberLiteral<Int>(value) {
            override val type = BuiltinType.I32Type
            override val className = "I32Lit"
        }
    }
}