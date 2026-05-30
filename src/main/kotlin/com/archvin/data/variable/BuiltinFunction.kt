package com.archvin.data.variable

import com.archvin.data.type.BuiltinType
import com.archvin.data.type.BuiltinType.*
import com.archvin.data.type.Type
import com.archvin.data.value.FunctionBody
import com.archvin.data.value.Value

sealed class BuiltinFunction(
    id: String,
    retType: BuiltinType<*>,
    paramTypes: List<BuiltinType<*>>,
    body: (List<Value>) -> Value)
    : Symbol.Function(id, Type.FunctionType(retType, paramTypes), FunctionBody.BuiltinFunction(body)
) {
    override fun getValue() = super.getValue() as FunctionBody.BuiltinFunction


    object Println : BuiltinFunction(
        "println",
        VoidType,
        listOf(DebugType),
        { args ->
            println(args[0].asString())
            Value.Uninitialized
        }
    )

    object Add : BuiltinFunction(
        "add",
        I32Type,
        listOf(I32Type, I32Type),
        { argsUncast ->
            val args = argsUncast as List<Value.PrimitiveValue<Int>>
            Value.PrimitiveValue(args[0].value + args[1].value)
        }
    )
}