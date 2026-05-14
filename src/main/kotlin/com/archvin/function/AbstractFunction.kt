package com.archvin.function

import com.archvin.debug.Debug
import com.archvin.instruction.Instruction
import com.archvin.type.BuiltinType.DebugType
import com.archvin.type.BuiltinType.VoidType
import com.archvin.type.HasType
import com.archvin.type.Type
import com.archvin.type.Type.FunctionType
import com.archvin.variable.Value

sealed class AbstractFunction(val returnType: Type, val paramTypes: List<Type>) : Debug(), HasType {
    override val type = FunctionType(paramTypes.map { it }.toList(), returnType)

    sealed class BuiltinFunction(type: Type, paramTypes: List<Type>) : AbstractFunction(type, paramTypes) {
        abstract fun call(args: List<Value>): Value

        object Println : BuiltinFunction(VoidType, listOf(DebugType)) {
            override fun call(args: List<Value>): Value {
                println(args[0].asString())
                return Value.Uninitialized
            }
        }
    }

    class FunctionObject(returnType: Type, paramTypes: List<Type>, val instructions: List<Instruction>)
        : AbstractFunction(returnType, paramTypes)
}