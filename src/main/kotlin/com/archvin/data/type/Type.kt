package com.archvin.data.type

import com.archvin.data.HasId
import com.archvin.data.type.BuiltinType.DebugType
import com.archvin.utils.Debug
import com.archvin.utils.funSignature

sealed class Type(val signature: String) : Debug() {
    sealed class ObjectType(override val id: String) : Type(id), HasId

        class FunctionType(val retType: Type, val paramTypes: List<Type>)
            : Type(funSignature(retType.signature, paramTypes.map { it.signature })) {
        override fun toString(): String = signature
    }

    override fun equals(other: Any?): Boolean {
        return if (other is DebugType || this is DebugType) true else this.signature == (other as? Type)?.signature
    }
}
