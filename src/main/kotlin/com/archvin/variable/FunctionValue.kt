package com.archvin.variable

import com.archvin.instruction.Instruction
import com.archvin.type.BuiltinType
import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.Type

sealed class FunctionValue(val returnType: Type, val paramTypes: List<Type>) : Value(Type.FunctionType(paramTypes, returnType)), HasType {
    override fun asString() = type.signature

    sealed class BuiltinFunction(override val id: String, type: Type, paramTypes: List<Type>) : FunctionValue(type, paramTypes),
        HasId {
        abstract fun call(args: List<Value>): Value

        object Println : BuiltinFunction("println", BuiltinType.VoidType, listOf(BuiltinType.DebugType)) {
            override fun call(args: List<Value>): Value {
                println(args[0].asString())
                return Value.Uninitialized
            }
        }
    }

    class CustomFunction(returnType: Type, paramTypes: List<Type>, val instructions: List<Instruction>)
        : FunctionValue(returnType, paramTypes)
}