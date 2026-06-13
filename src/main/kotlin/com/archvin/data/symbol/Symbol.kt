package com.archvin.data.symbol

import com.archvin.data.HasId
import com.archvin.data.HasType
import com.archvin.data.type.Type
import com.archvin.data.value.LambdaVal
import com.archvin.data.value.Value
import com.archvin.utils.Debug

open class Symbol protected constructor(
        override val id: String,
        override val type: Type,
        val isMutable: Boolean) : Debug(), HasId, HasType  {

    fun isFunction() = type is Type.FunctionType
    fun asFunction() = if (isFunction())  Function(id, type as Type.FunctionType, isMutable) else null

    companion object {
        fun createFun(id: String, retType: Type, paramTypes: List<Type>) =
            Symbol(id, Type.FunctionType(retType, paramTypes), false)

        fun createVar(id: String, type: Type, isMutable: Boolean = false) = Symbol(id, type, isMutable)

    }

    class Function(id: String,
                   override val type: Type.FunctionType,
                   isMutable: Boolean) : Symbol(id, type, isMutable)

}


