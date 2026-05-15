package com.archvin.variable

import com.archvin.debug.Debug
import com.archvin.type.BuiltinType
import com.archvin.type.HasType
import com.archvin.type.Type

sealed class Value(override val type: Type) : Debug(), HasType {
    abstract fun asString(): String // not the same as toString

    class PrimitiveValue<out T>(val value: T, type: BuiltinType<T>) : Value(type) {
        override fun asString(): String = value.toString()
    }

    object Uninitialized : Value(BuiltinType.DebugType) {
        // TODO: error when used
        override fun asString(): String = "Uninitialized"
    }
}