package com.archvin.pipeline.execution

import com.archvin.data.type.BuiltinType
import com.archvin.data.value.LambdaVal
import com.archvin.data.value.Value
import com.archvin.pipeline.IStage
import com.archvin.pipeline.finalizing.Instruction
import com.archvin.reader.Reader
import java.util.*

object Runner : IStage.IConsumer<Unit, Unit, Instruction> {
    val stack: Stack<Value> = Stack()
    override lateinit var r: Reader<Instruction>

    override fun ret() {}

    override fun consume(c: Instruction) {
        when (c) {
            is Instruction.LitInstr -> {
                stack.push(c.value)
            }
            is Instruction.ReadInstr -> {
                stack.push(c.variable.value)
            }
            is Instruction.AssignInstr -> {
                c.variable.value = stack.pop()
            }
            is Instruction.CallInstr -> {
                val func = c.function
                when (func.value) {
                    is LambdaVal.Builtin -> {
                        val params = func.type.paramTypes.indices.map { stack.pop() }
                            .reversed() // TODO: reverse at compile time

                        func.value.body(params)
                            .takeIf { func.type.retType !is BuiltinType.VoidType }?.let { stack.push(it) }
                    }
                    is LambdaVal.Composite -> func.value.instructions.forEach { consume(it) }
                }
            }
            is Instruction.PassInstr -> { /* Do nothing */ }
        }
    }
}