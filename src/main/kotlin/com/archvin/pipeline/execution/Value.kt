package com.archvin.pipeline.execution

import com.archvin.utils.Debug

sealed class Value : Debug() {
    abstract fun asString(): String

    class Primitive<out T>(val value: T) : Value() {
        override fun asString() = value.toString()
    }

    object Uninitialized : Value() {
        override fun asString() = "uninitialized"
    }

    class StaticValue { var value: Value = Uninitialized }
}