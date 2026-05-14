package com.archvin.process

import com.archvin.instruction.Instruction
import com.archvin.instruction.Instruction.*
import com.archvin.variable.Value
import java.util.*

class Runner : Processor<Unit, Instruction>() {
    val stack: Stack<Value> = Stack()

    override fun step(c: Instruction) {
        when (c) {
            is Literal<*> -> {
                stack.add(Value.PrimitiveValue(c.lit.value, c.lit.type))
            }
            is Read -> {
                stack.add(c.variable.value)
            }
            is Assign -> {
                c.variable.value = stack.pop()
            }
            is Println -> println(stack.pop().asString())
            is Pass -> { /* Do nothing */ }
        }
    }
}