package com.archvin.data

import com.archvin.data.type.BuiltinType
import com.archvin.utils.Debug

sealed class Literal<out T>(val value: T) : Debug(), HasType {
    abstract override val type: BuiltinType<T>

    class StringLiteral(value: String) : Literal<String>(value) {
        override val type = BuiltinType.StrType
    }

    class CharLiteral(value: Char) : Literal<Char>(value) {
        override val type = BuiltinType.CharType
    }

    sealed class NumberLiteral<T : Number>(value: T) : Literal<T>(value) {
        class I32Literal(value: Int) : NumberLiteral<Int>(value) {
            override val type = BuiltinType.I32Type
            override val className = "I32Lit"
        }
    }
}