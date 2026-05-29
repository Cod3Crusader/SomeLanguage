package com.archvin.variable

import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.Type

sealed class VariableLike(override val id: String,
                            override val type: Type) : HasId, HasType {
    // TODO: init; rename

    abstract fun getValue(): Value
    abstract fun setValue(value: Value)

    class Variable(id: String, type: Type, val isMutable: Boolean = true) : VariableLike(id, type) {
        private var value: Value = Value.Uninitialized

        override fun getValue() = value
        override fun setValue(value: Value) { this.value = value }
    }

    open class Function(id: String, override val type: Type.FunctionType) : VariableLike(id, type) {
        private lateinit var value: FunctionValue // TODO: init safety

        override fun getValue(): FunctionValue = value
        override fun setValue(value: Value) { this.value = value as FunctionValue }

    }
}


