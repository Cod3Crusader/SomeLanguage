package com.archvin.process

import com.archvin.instruction.Instruction
import com.archvin.instruction.Instruction.*
import com.archvin.type.BuiltinType.VoidType
import com.archvin.variable.FunctionValue
import com.archvin.variable.Value
import java.util.*

class Runner : Processor<Unit, Instruction>() {
    val stack: Stack<Value> = Stack()

    fun execute(instr: Instruction) {
        when (instr) {
            is Literal<*> -> {
                stack.push(Value.PrimitiveValue(instr.lit.value, instr.lit.type))
            }
            is Read -> {
                stack.push(instr.variable.value)
            }
            is Assign -> {
                instr.variable.value = stack.pop()
            }
            is Call -> {
                val func = instr.function
                if (func is FunctionValue.BuiltinFunction) {
                    val params = func.paramTypes.indices.map { stack.pop() }

                    func.call(params).takeIf { func.returnType !is VoidType }?.let { stack.push(it) }
                } else {
                    TODO("coming very soon")
                }
            }
            is Pass -> { /* Do nothing */ }
        }
    }

    override fun step(c: Instruction) {
        execute(c)
    }
}