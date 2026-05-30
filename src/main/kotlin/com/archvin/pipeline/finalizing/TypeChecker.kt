package com.archvin.pipeline.finalizing

import com.archvin.data.type.Type
import com.archvin.data.value.FunctionBody.CustomFunction
import com.archvin.data.variable.Symbol
import com.archvin.data.variable.Symbol.Variable
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.Stage
import com.archvin.pipeline.finalizing.Instruction.*
import com.archvin.pipeline.parsing.Expression
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.reader.Reader

class TypeChecker : Stage<Instruction, Expression>() {
    private val resolver = NameResolver()
    private val manager = InstructionManager()

    private val scopeStack = ArrayDeque<PendingScope>()

    fun declare(c: DeclareExpr) {
        if (c !is DeclareExpr.FunDeclare) {

            val type = resolver.resolveType(c.typeId)
            val variable = Variable(c.id, type, c.isMutable)
            resolver.add(variable)
            manager.addPending(AssignInstr(variable))
        } else {
            val type =
                 Type.FunctionType(
                    resolver.resolveType(c.retType),
                    c.paramTypes.map { resolver.resolveType(it) }
                )

            val lambda = r.step()
            if (lambda !is LambdaExpr) throw CompileError.UninitializedError(c.id)

            scopeStack.add(PendingScope())

            if (manager.hasPending()) error("useful message")
            for (i in 1 .. lambda.exprNum) {
                step(r.step())
            }
            if (manager.hasPending()) error("useful message")

            val variable = Symbol.Function.CustomFunction(
                c.id, type, CustomFunction(scopeStack.removeLast().instructions)
            )
            resolver.add(variable)
        }
    }

    override fun yield(add: Instruction) {
        if (scopeStack.isEmpty()) super.yield(add)
        else scopeStack.last().instructions.add(add)
    }

    override fun step(c: Expression) {
        when (c) {
            is ReadExpr -> manager.addComplete(ReadInstr(resolver.resolveVar(c.id)))
            is LitExpr<*> -> manager.addComplete(LitInstr(c.lit))
            is AssignExpr -> {
                val variable = resolver.resolveVar(c.variableId)
                manager.addPending(AssignInstr(variable))
                if (!variable.isMutable) throw CompileError.CannotReassign(variable)
            }
            is DeclareExpr -> declare(c)
            is CallExpr -> {
                val resolved = resolver.resolveFunc(c.functionId)
                val paramNum = resolved.type.paramTypes.size
                if (c.paramNum != paramNum) throw CompileError.InvalidArgumentCount(c.functionId, paramNum, c.paramNum)
                manager.addPending(CallInstr(resolved))
            }

            is LambdaExpr -> {

            }

            is OpExpr -> TODO()
            is PassExpr -> TODO("remove this sometime")
        }
    }

    override fun process(r: Reader<Expression>): List<Instruction> {
        val ret = super.process(r)

        if (manager.hasPending()) error("unfinished scope or instruction")

        return ret
    }

    private inner class InstructionManager {
        inner class PendingInstruction(val instr: Instruction, var counter: Int)

        private val instrStack = ArrayDeque<PendingInstruction>()

        fun peek() = instrStack.lastOrNull()
        fun hasPending() = instrStack.isNotEmpty()

        private fun checkType(instr: Instruction) {
            val top = peek() ?: return

            val types = top.instr.paramTypes
            instr.assertType(types[types.size - top.counter])
        }

        private fun tryPop() {
            if (peek()?.counter != 0) return

            val instr = instrStack.removeLastOrNull()!!.instr
            yield(instr)

            peek()?.counter--
            tryPop()
        }

        fun addPending(instr: Instruction) {
            checkType(instr)

            val counter = instr.paramTypes.size
            instrStack.add(PendingInstruction(instr, counter))

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

    private class PendingScope { val instructions = arrayListOf<Instruction>() }
}