package com.archvin.data.value

import com.archvin.data.scope.Scope
import com.archvin.pipeline.typecheck.Instruction

sealed class LambdaVal : Value() {
    override fun asString() = "function" //TODO

    class Builtin(val body: (List<Value>) -> Value) : LambdaVal()
    class Composite(val scope: Scope, val instructions: List<Instruction>, val level: Int) : LambdaVal()
}