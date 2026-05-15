package com.archvin.variable

import com.archvin.debug.Debug
import com.archvin.exceptions.CompileError
import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.Type

sealed class Variable(override val id: String, override val type: Type) : Debug(), HasId, HasType {
    abstract var value: Value
        protected set

    abstract fun changeValue(value: Value)

    class Mutable(id: String, type: Type) : com.archvin.variable.Variable(id, type) {
        override var value: Value = Value.Uninitialized
            set(newValue) {
                if (newValue.type != type) throw CompileError.TypeMismatchError(newValue.type, type)
                field = newValue
            }

        constructor(id: String, value: Value) : this(id, value.type) {
            this.value = value
        }

        override fun changeValue(value: Value) {
            this.value = value
        }
    }

    class Constant(id: String, override var value: Value) : com.archvin.variable.Variable(id, value.type) {
        override fun changeValue(value: Value) {
            throw CompileError.CannotReassign(this)
        }

        init {
            if (value == Value.Uninitialized) throw CompileError.UninitializedError(this)
        }
    }
}