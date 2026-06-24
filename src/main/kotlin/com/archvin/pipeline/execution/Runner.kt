package com.archvin.pipeline.execution

import com.archvin.data.value.LambdaVal
import com.archvin.data.value.Value
import com.archvin.pipeline.typecheck.Instruction

object Runner {

    private val tempStack = ArrayDeque<Value>()
    private val valueStack = ArrayList<ArrayDeque<MutableList<Value>>>()
    private val statics = ArrayList<Value>()

    private fun get(relativeLevel: Int, index: Int) = valueStack[valueStack.size - 1 - relativeLevel].last()[index]
    private fun set(relativeLevel: Int, index: Int, newValue: Value) {
       valueStack[valueStack.size - 1 - relativeLevel].last()[index] = newValue
    }

    private fun call(instr: Instruction.CallInstr) {
        when (val func = tempStack.removeLast() as LambdaVal) {
            is LambdaVal.Builtin -> {
                val params = List(instr.paramNum) { tempStack.removeLast() }
                    .reversed() // TODO: reverse at compile time
                func.body(params)
                    .takeIf { it != Value.Uninitialized }?.let { tempStack.add(it) }
            }
            is LambdaVal.Composite -> {
                if (valueStack.size == func.level) valueStack.add(ArrayDeque())
                else if (valueStack.size < func.level) error("this should be impossible")
                valueStack[func.level].add(MutableList(func.varNum) { Value.Uninitialized } )

                (0 until instr.paramNum).forEach { set(0, it, tempStack.removeLast()) }
                func.instructions.forEach { consume(it) }

                valueStack[func.level].removeLast()
                if (valueStack[func.level].isEmpty()) valueStack.removeLast()
            }
        }
    }

    fun consume(c: Instruction) {
        when (c) {
            is Instruction.ReadInstr -> tempStack.add(get(c.level, c.index))
            is Instruction.AssignInstr -> set(c.level, c.index, tempStack.removeLast())
            is Instruction.ReadStatic -> tempStack.add(c.static.value)
            is Instruction.AssignStatic -> c.static.value = tempStack.removeLast()

            is Instruction.LitInstr -> tempStack.add(c.value)
            is Instruction.CallInstr -> call(c)
        }
    }

    fun process(r: LambdaVal) {
        tempStack.add(r)
        call(Instruction.CallInstr(0))
    }
}