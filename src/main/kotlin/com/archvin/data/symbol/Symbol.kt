package com.archvin.data.symbol

import com.archvin.data.HasId
import com.archvin.data.HasType
import com.archvin.data.type.Type
import com.archvin.utils.Debug

open class Symbol(
    override val id: String,
    override val type: Type,
    val isMutable: Boolean = true) : Debug(), HasId, HasType  {

    open class Function(id: String, type: Type.FunctionType) : Symbol(id, type, false) {
        override val type: Type.FunctionType = type

        constructor(id: String, retType: Type, paramTypes: List<Type>) : this(id, Type.FunctionType(retType, paramTypes))
    }
}


