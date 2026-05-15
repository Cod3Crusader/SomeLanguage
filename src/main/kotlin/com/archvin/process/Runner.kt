package com.archvin.process

import com.archvin.instruction.Instruction
import com.archvin.instruction.Instruction.*
import com.archvin.variable.FunctionValue
import com.archvin.variable.Value
import java.util.*

class Runner : Processor<Unit, Instruction>() {
    val stack: Stack<Value> = Stack()

    fun execute(instr: Instruction) {
        when (instr) {
            is Literal<*> -> {
                stack.add(Value.PrimitiveValue(instr.lit.value, instr.lit.type))
            }
            is Read -> {
                stack.add(instr.variable.value)
            }
            is Assign -> {
                instr.variable.changeValue(stack.pop())
            }
            is Call -> {
                val func = instr.function
                if (func is FunctionValue.BuiltinFunction) {
                    assert(func.paramTypes.size == 1) // TODO
                    func.call(listOf(stack.pop()))
                } else {
                    TODO("coming very soon")
                }
            }
            is Println -> println(stack.pop().asString())
            is Pass -> { /* Do nothing */ }
        }
    }

    override fun step(c: Instruction) {
        execute(c)
    }
}