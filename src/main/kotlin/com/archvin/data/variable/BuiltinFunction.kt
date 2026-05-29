package com.archvin.data.variable

import com.archvin.data.type.BuiltinType
import com.archvin.data.type.Type
import com.archvin.data.value.Value

sealed class BuiltinFunction(id: String, retType: BuiltinType<*>, paramTypes: List<BuiltinType<*>>) : Symbol.Function(id, Type.FunctionType(retType, paramTypes)) {

    abstract fun call(args: List<Value>): Value

    object Println : BuiltinFunction(
        "println",
        BuiltinType.VoidType,
        listOf(BuiltinType.DebugType)
    ) {

        override fun call(args: List<Value>): Value {
            println(args[0])
            return Value.Uninitialized
        }
    }

    object Add : BuiltinFunction(
        "add",
        BuiltinType.I32Type,
        listOf(BuiltinType.I32Type, BuiltinType.I32Type)
    ) {
        // TODO: remove

        override fun call(args: List<Value>): Value {
            val toAdd = args as List<Value.PrimitiveValue<Int>>
            return Value.PrimitiveValue(toAdd[0].value + toAdd[1].value)
        }

    }
}