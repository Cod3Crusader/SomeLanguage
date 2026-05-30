package com.archvin.pipeline.execution

import com.archvin.data.type.BuiltinType
import com.archvin.data.value.Value
import com.archvin.data.variable.BuiltinFunction
import com.archvin.data.variable.Symbol
import com.archvin.pipeline.Stage
import com.archvin.pipeline.finalizing.Instruction
import java.util.*

class Runner : Stage<Unit, Instruction>() {
    val stack: Stack<Value> = Stack()

    fun execute(instr: Instruction) {
        when (instr) {
            is Instruction.LitInstr -> {
                stack.push(instr.value)
            }
            is Instruction.ReadInstr -> {
                stack.push(instr.variable.getValue())
            }
            is Instruction.AssignInstr -> {
                instr.variable.setValue(stack.pop())
            }
            is Instruction.CallInstr -> {
                when (val func = instr.function) {
                    is BuiltinFunction -> {
                        val params = func.type.paramTypes.indices.map { stack.pop() }
                            .reversed() // TODO: reverse at compile time

                        func.getValue().body(params)
                            .takeIf { func.type.retType !is BuiltinType.VoidType }?.let { stack.push(it) }
                    }
                    is Symbol.Function.CustomFunction -> func.getValue().instructions.forEach { execute(it) }
                }
            }
            is Instruction.PassInstr -> { /* Do nothing */ }
        }
    }

    override fun step(c: Instruction) {
        execute(c)
    }
}