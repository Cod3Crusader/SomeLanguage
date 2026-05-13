package com.archvin.type

import com.archvin.function.FunctionObject
import com.archvin.type.BuiltinType.DebugType
import com.archvin.variable.Variable

sealed class Type {
    data class ObjectType(
            override val id: String,
            val properties: List<Variable>,
            val functions: List<FunctionObject>) : Type(), HasId {

        override fun toString(): String = id
    }

    data class FunctionType(val paramTypes: List<Type>, val returnType: Type) : Type() {
        override fun toString(): String = "(${paramTypes.joinToString()}):($returnType)"
    }

    fun matches(other: Type) = if (other is DebugType || this is DebugType) true else this == other
}
