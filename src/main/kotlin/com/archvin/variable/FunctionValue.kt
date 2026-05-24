package com.archvin.variable

import com.archvin.instruction.Instruction
import com.archvin.type.BuiltinType
import com.archvin.type.BuiltinType.I32Type
import com.archvin.type.HasType
import com.archvin.type.Type

sealed class FunctionValue(val returnType: Type, val paramTypes: List<Type>) : Value(Type.FunctionType(paramTypes, returnType)), HasType {
    override fun asString() = type.signature

    sealed class BuiltinFunction(returnType: Type, paramTypes: List<Type>)
            : FunctionValue(returnType, paramTypes) {
        abstract fun call(args: List<Value>): Value

        object Println : BuiltinFunction(BuiltinType.VoidType, listOf(BuiltinType.DebugType)) {
            override fun call(args: List<Value>): Value {
                println(args[0].asString())
                return Uninitialized
            }
        }

        object Add : BuiltinFunction(I32Type, listOf(I32Type, I32Type)) {
            // TODO: remove

            override fun call(args: List<Value>): Value {
                val toAdd = args as List<PrimitiveValue<Int>>
                return PrimitiveValue(toAdd[0].value + toAdd[1].value, I32Type)
            }

        }
    }

    class CustomFunction(returnType: Type, paramTypes: List<Type>, val instructions: List<Instruction>)
        : FunctionValue(returnType, paramTypes)
}