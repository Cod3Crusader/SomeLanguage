package com.archvin.pipeline.execution

import com.archvin.pipeline.typecheck.Instruction
import com.archvin.pipeline.typecheck.TypeChecker

object Runner {

    private fun call(instr: Instruction.CallInstr): Value {
        when (val func = consume(instr.func) as LambdaVal) {
            is LambdaVal.Builtin -> {
                val params = instr.params.map { consume(it) }
                func.body(params)
                    .takeIf { it != Value.Uninitialized }?.let { return it }

                return Value.Uninitialized // return if the function returns nothing
            }
            is LambdaVal.Composite -> {
                val scope = func.scope
                scope.incDepth()

                instr.params.forEachIndexed { index, it -> scope[index] = consume(it) }
                func.instructions.forEach { consume(it) }

                scope.decDepth()

                return Value.Uninitialized // TODO
            }
        }
    }

    fun consume(c: Instruction): Value {
        when (c) {
            is Instruction.ReadInstr -> return c.scope[c.index]
            is Instruction.AssignInstr -> c.scope[c.index] = consume(c.newValue)

            is Instruction.LoadValue -> return c.value
            is Instruction.CallInstr -> return call(c)
        }

        return Value.Uninitialized
    }

    fun process(r: LambdaVal) {
        TypeChecker.topScope.incDepth()
        call(Instruction.CallInstr(Instruction.LoadValue(r), emptyList()))
        TypeChecker.topScope.decDepth()
    }
}