package com.archvin.data.symbol

import com.archvin.data.HasId
import com.archvin.data.HasType
import com.archvin.data.type.Type
import com.archvin.data.value.Value
import com.archvin.utils.Debug

open class Symbol protected constructor(
        override val id: String,
        override val type: Type,
        val isMutable: Boolean) : Debug(), HasId, HasType  {

    fun isFunction() = type is Type.FunctionType
    fun asFunction() = if (isFunction())  Function(this, type as Type.FunctionType) else null

    companion object {
        fun createFun(id: String, retType: Type, paramTypes: List<Type>) =
            StaticSymbol(id, Type.FunctionType(retType, paramTypes), isMutable = false)

        fun createVar(id: String, type: Type, isMutable: Boolean = false) = Symbol(id, type, isMutable)

    }

    class Function(val symbol: Symbol, override val type: Type.FunctionType) : Debug(), HasId, HasType {
        override val id = symbol.id
    }

    open class StaticSymbol(id: String, type: Type, isMutable: Boolean) : Symbol(id, type, isMutable) {
        val value = Value.StaticValue()
        fun load(newValue: Value) { value.value = newValue }
    }
}


