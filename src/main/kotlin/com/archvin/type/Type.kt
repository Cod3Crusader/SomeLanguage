package com.archvin.type

import com.archvin.debug.Debug
import com.archvin.type.BuiltinType.DebugType

sealed class Type : Debug() {
    sealed class ObjectType(override val id: String) : Type(), HasId

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
