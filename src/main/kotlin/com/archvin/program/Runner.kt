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
            is Instruction.Literal<*> -> {
                stack.push(Value.PrimitiveValue(instr.lit.value, instr.lit.type))
            }
            is Instruction.Read -> {
                stack.push(instr.variable.value)
            }
            is Instruction.Assign -> {
                instr.variable.value = stack.pop()
            }
            is Instruction.Call -> {
                val func = instr.function
                if (func is FunctionValue.BuiltinFunction) {
                    val params = func.paramTypes.indices.map { stack.pop() }

                    func.call(params).takeIf { func.returnType !is BuiltinType.VoidType }?.let { stack.push(it) }
                } else {
                    TODO("coming very soon")
                }
            }
            is Instruction.Pass -> { /* Do nothing */ }
        }
    }

    override fun step(c: Instruction) {
        execute(c)
    }
}