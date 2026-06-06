package com.archvin.data.variable

import com.archvin.data.type.BuiltinType
import com.archvin.data.type.BuiltinType.*
import com.archvin.data.type.Type
import com.archvin.data.value.LambdaVal
import com.archvin.data.value.Value

sealed class BuiltinFunction(
    id: String,
    retType: BuiltinType<*>,
    paramTypes: List<BuiltinType<*>>,
    body: (List<Value>) -> Value
) : Symbol.Function(id, Type.FunctionType(retType, paramTypes)) {

    init {
        setValue(LambdaVal.Builtin(body))
    }

    override fun getValue() = super.getValue() as LambdaVal.Builtin


    object Println : BuiltinFunction(
        "println",
        VoidType,
        listOf(AnyType),
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
            val args = argsUncast as List<Value.Primitive<Int>>
            Value.Primitive(args[0].value + args[1].value)
        }
    )
}