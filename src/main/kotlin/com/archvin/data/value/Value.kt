package com.archvin.data.value

import com.archvin.utils.Debug

sealed class Value : Debug() {

    class PrimitiveValue<out T>(val value: T) : Value()

    object Uninitialized : Value()
}