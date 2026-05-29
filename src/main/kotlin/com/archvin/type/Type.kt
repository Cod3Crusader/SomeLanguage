package com.archvin.type

import com.archvin.type.BuiltinType.DebugType
import com.archvin.utils.Debug

sealed class Type(val signature: String) : Debug() {
    sealed class ObjectType(override val id: String) : Type(id), HasId

        class FunctionType(val returnType: Type, val paramTypes: List<Type>) : Type("(${paramTypes.joinToString()}):($returnType)") {
        override fun toString(): String = signature
    }

    override fun equals(other: Any?): Boolean {
        return if (other is DebugType || this is DebugType) true else this.signature == (other as? Type)?.signature
    }
}
