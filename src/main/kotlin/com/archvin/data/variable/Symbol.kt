package com.archvin.data.variable

import com.archvin.data.HasId
import com.archvin.data.HasType
import com.archvin.data.type.Type
import com.archvin.data.value.LambdaVal
import com.archvin.data.value.Value
import com.archvin.utils.Debug

open class Symbol protected constructor(
        override val id: String,
        override val type: Type,
        val isMutable: Boolean,
        value: Value = Value.Uninitialized) : Debug(), HasId, HasType  {

    var value: Value = value
        set(newValue) {
            if (!isMutable && field !is Value.Uninitialized) error("symbol $id cannot be mutated")
            field = newValue
        }


    fun asFunction(): Function? {
        if (type !is Type.FunctionType) return null
        if (value is Value.Uninitialized) return null

        return Function(value as LambdaVal, type as Type.FunctionType)
    }

    companion object {
        fun createFun(id: String, retType: Type, paramTypes: List<Type>) =
            Symbol(id, Type.FunctionType(retType, paramTypes), false)

        fun createVar(id: String, type: Type, isMutable: Boolean = false) = Symbol(id, type, isMutable)

    }

    class Function(val value: LambdaVal, val type: Type.FunctionType)
}


