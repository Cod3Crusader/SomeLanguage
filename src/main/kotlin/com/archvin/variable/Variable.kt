package com.archvin.variable

import com.archvin.exceptions.CompileError
import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.Type
import com.archvin.utils.Debug

sealed class Variable(override val id: String, override val type: Type) : Debug(), HasId, HasType {
    abstract var value: Value

    class Mutable(id: String, type: Type) : Variable(id, type) {
        override var value: Value = Value.Uninitialized
            set(newValue) {
                if (newValue.type != type) throw CompileError.TypeMismatchError(newValue.type, type)
                field = newValue
            }

        constructor(id: String, value: Value) : this(id, value.type) {
            this.value = value
        }
    }

    class Constant(id: String, override var value: Value) : Variable(id, value.type) {
        init {
            if (value == Value.Uninitialized) throw CompileError.UninitializedError(this)
        }
    }
}