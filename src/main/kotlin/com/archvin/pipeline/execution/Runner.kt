package com.archvin.pipeline.execution

import com.archvin.data.type.BuiltinType
import com.archvin.data.value.Value
import com.archvin.data.variable.BuiltinFunction
import com.archvin.data.variable.Symbol
import com.archvin.pipeline.Stage
import com.archvin.pipeline.finalizing.Instruction
import java.util.*

class Runner : Stage.ConsumerStage<Unit, Instruction>() {
    val stack: Stack<Value> = Stack()

    override fun consume(c: Instruction) {
        when (c) {
            is Instruction.LitInstr -> {
                stack.push(c.value)
            }
            is Instruction.ReadInstr -> {
                stack.push(c.variable.getValue())
            }
            is Instruction.AssignInstr -> {
                c.variable.setValue(stack.pop())
            }
            is Instruction.CallInstr -> {
                when (val func = c.function) {
                    is BuiltinFunction -> {
                        val params = func.type.paramTypes.indices.map { stack.pop() }
                            .reversed() // TODO: reverse at compile time

                        func.getValue().body(params)
                            .takeIf { func.type.retType !is BuiltinType.VoidType }?.let { stack.push(it) }
                    }
                    is Symbol.Function.CustomFunction -> func.getValue().instructions.forEach { consume(it) }
                }
            }
            is Instruction.PassInstr -> { /* Do nothing */ }
        }
    }
}