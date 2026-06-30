package com.archvin.data.symbol

import com.archvin.data.type.BuiltinType
import com.archvin.data.type.BuiltinType.*
import com.archvin.pipeline.execution.LambdaVal
import com.archvin.pipeline.execution.Value

class BuiltinFunction private constructor(
    id: String,
    retType: BuiltinType<*>,
    paramTypes: List<BuiltinType<*>>,
    body: (List<Value>) -> Value
) : Symbol.Function(id, retType, paramTypes) {

    val lambda: LambdaVal.Builtin = LambdaVal.Builtin(body)

    init {
        builtins.add(this)
    }

    companion object {
        val builtins = ArrayList<BuiltinFunction>()

        val print = BuiltinFunction(
            "print",
            VoidType,
            listOf(AnyType)
        ) { args ->
            print(args[0].asString())
            Value.Uninitialized
        }

        val add = BuiltinFunction(
            "add",
            I32Type,
            listOf(I32Type, I32Type)
        ) { argsUncast ->
            val args = argsUncast as List<Value.Primitive<Int>>
            Value.Primitive(args[0].value + args[1].value)
        }
    }

}