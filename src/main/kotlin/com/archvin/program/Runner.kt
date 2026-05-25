package com.archvin.program

import com.archvin.Processor
import com.archvin.type.BuiltinType
import com.archvin.variable.FunctionValue
import com.archvin.variable.Value
import java.util.*

class Runner : Processor<Unit, Instruction>() {
    val stack: Stack<Value> = Stack()

    fun execute(instr: Instruction) {
        when (instr) {
            is Instruction.LitInstr<*> -> {
                stack.push(Value.PrimitiveValue(instr.lit.value, instr.lit.type))
            }
            is Instruction.ReadInstr -> {
                stack.push(instr.variable.value)
            }
            is Instruction.AssignInstr -> {
                instr.variable.value = stack.pop()
            }
            is Instruction.CallInstr -> {
                val func = instr.function
                if (func is FunctionValue.BuiltinFunction) {
                    val params = func.paramTypes.indices.map { stack.pop() }.reversed()
                    // TODO: reverse at compile time

                    func.call(params).takeIf { func.returnType !is BuiltinType.VoidType }?.let { stack.push(it) }
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