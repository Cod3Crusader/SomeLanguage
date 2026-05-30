package com.archvin.data.variable

import com.archvin.data.HasId
import com.archvin.data.HasType
import com.archvin.data.type.Type
import com.archvin.data.value.LambdaVal
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

    sealed class Function(id: String, override val type: Type.FunctionType) : Symbol(id, type) {
        private var body: LambdaVal? = null

        override fun getValue(): LambdaVal = body!!
        override fun setValue(value: Value) { body = value as LambdaVal }

        class CustomFunction(id: String, type: Type.FunctionType) : Function(id, type) {
            override fun getValue() = super.getValue() as LambdaVal.Composite
        }
    }
}


