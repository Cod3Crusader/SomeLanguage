package com.archvin.data.variable

import com.archvin.data.HasId
import com.archvin.data.HasType
import com.archvin.data.type.Type
import com.archvin.data.value.FunctionBody
import com.archvin.data.value.Value
import com.archvin.exceptions.CompileError
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

    sealed class Function(id: String, override val type: Type.FunctionType, body: FunctionBody) : Symbol(id, type) {
        private val value = body

        override fun getValue(): FunctionBody = value
        override fun setValue(value: Value) = throw CompileError.CannotReassign(this)

        class CustomFunction(id: String, type: Type.FunctionType, body: FunctionBody.CustomFunction) : Function(id, type, body) {
            override fun getValue() = super.getValue() as FunctionBody.CustomFunction
        }
    }
}


