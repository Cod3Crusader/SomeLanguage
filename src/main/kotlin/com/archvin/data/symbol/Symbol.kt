package com.archvin.data.symbol

import com.archvin.data.HasId
import com.archvin.data.HasType
import com.archvin.data.type.Type
import com.archvin.utils.Debug

open class Symbol protected constructor(
        override val id: String,
        override val type: Type,
        val isMutable: Boolean) : Debug(), HasId, HasType  {

    companion object {
        fun create(id: String, type: Type, isMutable: Boolean = true): Symbol {
            // TODO: reconsider function mutability
            return if (type is Type.FunctionType && !isMutable) Function(id, type)
            else Symbol(id, type, isMutable)
        }

        fun createFun(id: String, retType: Type, paramTypes: List<Type>) = create(id, Type.FunctionType(retType, paramTypes), false)
    }

    open class Function(id: String, override val type: Type.FunctionType) : Symbol(id, type, false)
}


