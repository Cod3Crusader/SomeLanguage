package com.archvin.type

import com.archvin.function.FunctionObject
import com.archvin.type.BuiltinType.DebugType
import com.archvin.variable.Variable
import com.debug.DebugString

sealed class Type : DebugString() {
    class ObjectType(
            override val id: String,
            val properties: List<Variable>,
            val functions: List<FunctionObject>) : Type(), HasId {

        override fun toString(): String = id
    }

    class FunctionType(val paramTypes: List<Type>, val returnType: Type) : Type() {
        override fun toString(): String = "(${paramTypes.joinToString()}):($returnType)"

        override fun equals(other: Any?): Boolean {
            return super.equals(other) || (other is FunctionType && paramTypes == other.paramTypes && returnType == other.returnType)
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is DebugType || this is DebugType) true else this === other
    }
}
