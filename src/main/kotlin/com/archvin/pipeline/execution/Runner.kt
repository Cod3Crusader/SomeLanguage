package com.archvin.pipeline.execution

import com.archvin.pipeline.typecheck.Instruction
import com.archvin.pipeline.typecheck.TypeChecker

object Runner {
    private fun executeScope(instructions: List<Instruction>, scope: RuntimeScope, params: List<Instruction> = emptyList()) : Value {
        scope.incDepth()

        params.forEachIndexed { index, it -> scope[index] = consume(it) }

        var result: Value = Value.Uninitialized
        for (instruction in instructions) {
            result = consume(instruction)
            if (result is Value.ReturnVal) { // TODO: check inside consume
                if (result.retFrom == scope) result = result.value // unpack if this is the desired scope, bubble up otherwise
                break
            }
        }

        scope.decDepth()

        return result
    }

    private fun call(instr: Instruction.CallInstr): Value {
        return when (val func = consume(instr.func) as LambdaVal) {
            is LambdaVal.Builtin -> {
                val params = instr.params.map { consume(it) }
                func.body(params)
            }
            is LambdaVal.Composite -> executeScope(func.instructions, func.scope, instr.params)
        }
    }

    fun consume(c: Instruction): Value = when (c) {
        is Instruction.ReadInstr -> c.scope[c.index]
        is Instruction.AssignInstr -> {
            c.scope[c.index] = consume(c.newValue)
            Value.Uninitialized
        }

        is Instruction.LoadValue -> c.value
        is Instruction.CallInstr -> call(c)

        is Instruction.LambdaInstr -> executeScope(c.body.instructions, c.body.scope)

        is Instruction.ReturnInstr -> Value.ReturnVal(c.returns?.let { consume(it) } ?: Value.Uninitialized, c.returnFrom)

        is Instruction.ConditionalInstr -> {
            val condition = consume(c.condition)

            if (condition is Value.Primitive<*> && condition.value == true) consume(c.body)
            else c.elseBranch?.let { consume(it) } ?: Value.Uninitialized
        }
    }

    fun process(r: LambdaVal) {
        TypeChecker.topScope.incDepth()
        call(Instruction.CallInstr(Instruction.LoadValue(r), emptyList()))
        TypeChecker.topScope.decDepth()
    }
}