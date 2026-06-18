package com.archvin.pipeline.execution

import com.archvin.data.value.LambdaVal
import com.archvin.data.value.Value
import com.archvin.pipeline.IStage
import com.archvin.pipeline.finalizing.Instruction
import com.archvin.reader.Reader
import com.archvin.utils.pop

object Runner : IStage.IConsumer<Unit, Unit, Instruction> {
    override lateinit var r: Reader<Instruction>

    override fun ret() {}

    private val tempStack = ArrayDeque<Value>()
    private val valueStack = ArrayList<ArrayDeque<MutableList<Value>>>()
    
    private fun get(level: Int, index: Int) = valueStack[level].last()[index]
    private fun set(level: Int, index: Int, newValue: Value) {
       valueStack[level].last()[index] = newValue
    }

    private fun call(instr: Instruction.CallInstr) {
        when (val func = tempStack.pop() as LambdaVal) {
            is LambdaVal.Builtin -> {
                val params = List(instr.paramNum) { tempStack.pop() }
                    .reversed() // TODO: reverse at compile time
                func.body(params)
                    .takeIf { it != Value.Uninitialized }?.let { tempStack.add(it) }
            }
            is LambdaVal.Composite -> {
                valueStack[func.level].add(MutableList(func.varNum) { Value.Uninitialized } )
                
                (0 until instr.paramNum).forEach { set(0, it, tempStack.pop()) }
                func.instructions.forEach { consume(it) }
                
                valueStack[func.level].pop()
            }
        }
    }

    override fun consume(c: Instruction) {
        when (c) {
            is Instruction.LitInstr -> tempStack.add(c.value)
            is Instruction.ReadInstr -> tempStack.add(get(c.level, c.index))
            is Instruction.AssignInstr -> set(c.level, c.index, tempStack.pop())
            is Instruction.CallInstr -> call(c)
        }
    }
}