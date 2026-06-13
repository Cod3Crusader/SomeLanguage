package com.archvin.data.value

import com.archvin.pipeline.finalizing.Instruction

sealed class LambdaVal : Value() {
    override fun asString() = "function" //TODO

    class Builtin(val body: (List<Value>) -> Value) : LambdaVal()
    class Composite(val instructions: MutableList<Instruction>) : LambdaVal()
}