package com.archvin.pipeline.execution

import com.archvin.pipeline.typecheck.Instruction

sealed class LambdaVal : Value() {
    override fun asString() = "function" //TODO

    class Builtin(val body: (List<Value>) -> Value) : LambdaVal()
    class Composite(val scope: RuntimeScope, val instructions: List<Instruction>) : LambdaVal()
}