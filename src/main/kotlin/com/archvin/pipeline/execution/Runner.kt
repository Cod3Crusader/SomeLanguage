package com.archvin.pipeline.execution

import com.archvin.pipeline.typecheck.Instruction

object Runner {

    private val tempStack = ArrayDeque<Value>()

    private fun call(instr: Instruction.CallInstr) {
        when (val func = tempStack.removeLast() as LambdaVal) {
            is LambdaVal.Builtin -> {
                val params = List(instr.paramNum) { tempStack.removeLast() }
                    .reversed() // TODO: reverse at compile time
                func.body(params)
                    .takeIf { it != Value.Uninitialized }?.let { tempStack.add(it) }
            }
            is LambdaVal.Composite -> {
                val scope = func.scope
                scope.incDepth()

                (0 until instr.paramNum).forEach { scope[it] = tempStack.removeLast() }
                func.instructions.forEach { consume(it) }

                scope.decDepth()
            }
        }
    }

    fun consume(c: Instruction) {
        when (c) {
            is Instruction.ReadInstr -> tempStack.add(c.scope[c.index])
            is Instruction.AssignInstr -> c.scope[c.index] = tempStack.removeLast()

            is Instruction.LoadValue -> tempStack.add(c.value)
            is Instruction.CallInstr -> call(c)
        }
    }

    fun process(r: LambdaVal) {
        tempStack.add(r)
        call(Instruction.CallInstr(0))
    }
}