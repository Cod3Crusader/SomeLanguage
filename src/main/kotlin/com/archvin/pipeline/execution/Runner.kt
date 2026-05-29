package com.archvin.pipeline.execution

import com.archvin.pipeline.Stage
import com.archvin.pipeline.finalizing.Instruction
import com.archvin.type.BuiltinType
import com.archvin.variable.BuiltinFunction
import com.archvin.variable.Value
import java.util.*

class Runner : Stage<Unit, Instruction>() {
    val stack: Stack<Value> = Stack()

    fun execute(instr: Instruction) {
        when (instr) {
            is Instruction.LitInstr<*> -> {
                stack.push(Value.PrimitiveValue(instr.lit.value))
            }
            is Instruction.ReadInstr -> {
                stack.push(instr.variable.getValue())
            }
            is Instruction.AssignInstr -> {
                instr.variable.setValue(stack.pop())
            }
            is Instruction.CallInstr -> {
                val func = instr.function
                if (func is BuiltinFunction) {
                    val params = func.type.paramTypes.indices.map { stack.pop() }.reversed()
                    // TODO: reverse at compile time

                    func.call(params).takeIf { func.type.retType !is BuiltinType.VoidType }?.let { stack.push(it) }
                } else {
                    TODO("coming very soon")
                }
            }
            is Instruction.PassInstr -> { /* Do nothing */ }
        }
    }

    override fun step(c: Instruction) {
        execute(c)
    }
}