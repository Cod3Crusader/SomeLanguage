package com.archvin.pipeline.execution

import com.archvin.data.type.BuiltinType
import com.archvin.data.value.LambdaVal
import com.archvin.data.value.Value
import com.archvin.pipeline.IStage
import com.archvin.pipeline.finalizing.Instruction
import com.archvin.reader.Reader
import java.util.*

object Runner : IStage.IConsumer<Unit, Unit, Instruction> {
    override lateinit var r: Reader<Instruction>

    override fun ret() {}

    private val tempStack: Stack<Value> = Stack()
    private val valueStack: ArrayList<ArrayDeque<MutableList<Value>>()

    val currentLevel = 0
    private fun get(relativeLevel: Int, index: Int) = valueStack[level - relativeLevel].last()[index]
    private fun set(relativeLevel: Int, index: Int, newValue: Value) {
       valueStack[level - relativeLevel].last()[index] = newValue
    }

    private fun call(instr: CallInstr) {
        val func = tempStack.removeLast() as LambdaVal
        when (func) {
            is LambdaVal.Builtin -> {
                val params = List(instr.paramNum) { tempStack.pop() }
                    .reversed() // TODO: reverse at compile time
                func.body(params)
                    .takeIf { it != Value.Uninitialized }?.let { tempStack.push(it) }
            }
            is LambdaVal.Composite -> {
                currentLevel -= instr.level
                valueStack[currentLevel].add(buildList(func.varNum) { Value.Uninitialized } )
                
                (0 until instr.paramNum).forEach { set(0, it, tempStack.removeLast()) }
                func.instructions.forEach { consume(it) }
                
                valueStack[currentLevel].removeLast()
            }
        }
    }

    override fun consume(c: Instruction) {
        when (c) {
            is Instruction.LitInstr -> tempStack.push(c.value)
            is Instruction.ReadInstr -> tempStack.push(c.variable.value)
            is Instruction.AssignInstr -> c.variable.value = tempStack.removeLast()
            is Instruction.CallInstr -> call(c)
        }
    }
}