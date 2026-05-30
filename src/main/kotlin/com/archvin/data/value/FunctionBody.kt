package com.archvin.data.value

import com.archvin.pipeline.finalizing.Instruction

sealed class FunctionBody : Value() {
    override fun asString() = "function" //TODO

    class BuiltinFunction(val body: (List<Value>) -> Value) : FunctionBody()

    class CustomFunction(val instructions: List<Instruction>) : FunctionBody()
}