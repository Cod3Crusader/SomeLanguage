package com.archvin.parser

import com.archvin.exceptions.CompileError
import com.archvin.expression.Expression
import com.archvin.instruction.Instruction
import com.archvin.process.Processor
import com.archvin.reader.Reader

class TypeChecker(private val resolver : Resolver) : Processor<Instruction, Expression>() {
    private val manager = InstructionManager()

    override fun step(c: Expression) {
        when (c) {
            is Expression.ReadExpr -> manager.addComplete(Instruction.ReadInstr(resolver.resolveVar(c.id)))
            is Expression.LitExpr<*> -> manager.addComplete(Instruction.LitInstr(c.lit))
            is Expression.AssignExpr -> manager.addPending(Instruction.AssignInstr(resolver.resolveVar(c.variableId)))
            is Expression.CallExpr -> {
                val resolved = resolver.resolveFunc(c.functionId)
                val paramNum = resolved.paramTypes.size
                if (c.paramNum != paramNum) throw CompileError.InvalidArgumentCount(c.functionId, paramNum, c.paramNum)
                manager.addPending(Instruction.CallInstr(resolved))
            }
            is Expression.OpExpr -> {}
            is Expression.PassExpr -> TODO("remove this sometime")
        }
    }

    override fun process(r: Reader<Expression>): List<Instruction> {
        val ret = super.process(r)

        if (manager.hasPending()) throw CompileError.UnfinishedInstruction(manager.peek()!!.instr)

        return ret
    }

    private inner class InstructionManager {
        inner class PendingInstruction(val instr: Instruction, var counter: Int)

        private val pending = ArrayDeque<PendingInstruction>()

        fun peek() = pending.lastOrNull()

        fun hasPending() = pending.isNotEmpty()

        private fun checkType(instr: Instruction) {
            val top = peek() ?: return

            val types = top.instr.paramTypes
            instr.assertType(types[types.size - top.counter])
        }

        private fun tryPop() {
            if (peek()?.counter != 0) return

            val instr = pending.removeLastOrNull()!!.instr
            yield(instr)

            peek()?.counter--
            tryPop()
        }

        fun addPending(instr: Instruction) {
            checkType(instr)

            val counter = instr.paramTypes.size
            pending.add(PendingInstruction(instr, counter))

           tryPop()
        }

        fun addComplete(instr: Instruction) {
            assert(instr.paramTypes.isEmpty())

            checkType(instr)

            yield(instr)

            peek()?.counter--
            tryPop()
        }
    }
}