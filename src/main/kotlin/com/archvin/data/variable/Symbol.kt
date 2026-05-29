package com.archvin.data.variable

import com.archvin.data.HasId
import com.archvin.data.HasType
import com.archvin.data.type.Type
import com.archvin.data.value.FunctionValue
import com.archvin.data.value.Value
import com.archvin.utils.Debug

sealed class Symbol(override val id: String,
                    override val type: Type) : Debug(), HasId, HasType {
    // TODO: init; rename

    abstract fun getValue(): Value
    abstract fun setValue(value: Value)

    class Variable(id: String, type: Type, val isMutable: Boolean = true) : Symbol(id, type) {
        private var value: Value = Value.Uninitialized

        override fun getValue() = value
        override fun setValue(value: Value) { this.value = value }
    }

    open class Function(id: String, override val type: Type.FunctionType) : Symbol(id, type) {
        private lateinit var value: FunctionValue // TODO: init safety

        override fun getValue(): FunctionValue = value
        override fun setValue(value: Value) { this.value = value as FunctionValue
        }

    }
}


