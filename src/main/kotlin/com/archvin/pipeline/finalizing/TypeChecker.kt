package com.archvin.pipeline.finalizing

import com.archvin.exceptions.CompileError
import com.archvin.pipeline.Stage
import com.archvin.pipeline.parsing.Expression
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.reader.Reader
import com.archvin.type.Type
import com.archvin.variable.VariableLike.Variable

class TypeChecker : Stage<Instruction, Expression>() {
    private val resolver = NameResolver()
    private val manager = InstructionManager()

    /*
    private fun parseHeader(header: Header) {
        for (element in header.elements) {
            when (element) {
                is HeaderElement.FunctionDeclaration -> {
                    val returnType = resolver.resolveType(element.returnId)
                    val paramTypes = element.paramTypeIds.map { resolver.resolveType(it) }
                    resolver.add(Variable(element.functionId, FunctionValue.CustomFunction(returnType, paramTypes, emptyList())))
                    // TODO: instructions
                }
            }
        }
    }
    *
     */

    /*
    private fun <T : HasId> String.resolve(): T {
        @Suppress("UNCHECKED_CAST")
        return resolver.tryResolve(this) as? T ?: throw CompileError.UnresolvedIdentifier(this)
    }
     */

    fun declare(c: DeclareExpr) {
        if (c !is DeclareExpr.FunDeclare) {

            val type = resolver.resolveType(c.typeId)
            val variable = Variable(c.id, type, c.isMutable)
            resolver.add(variable)
            manager.addPending(Instruction.AssignInstr(variable))
        } else {
            val type =
                 Type.FunctionType(
                    resolver.resolveType(c.retType),
                    c.paramTypes.map { resolver.resolveType(it) }
                )

            val variable = Variable(c.id, type, false)
            resolver.add(variable)
        }
    }

    override fun step(c: Expression) {
        when (c) {
            is ReadExpr -> manager.addComplete(Instruction.ReadInstr(resolver.resolveVar(c.id)))
            is LitExpr<*> -> manager.addComplete(Instruction.LitInstr(c.lit))
            is AssignExpr -> {
                val variable = resolver.resolveVar(c.variableId)
                manager.addPending(Instruction.AssignInstr(variable))
                if (!variable.isMutable) throw CompileError.CannotReassign(variable)
            }
            is DeclareExpr -> declare(c)
            is CallExpr -> {
                val resolved = resolver.resolveFunc(c.functionId)
                val paramNum = resolved.type.paramTypes.size
                if (c.paramNum != paramNum) throw CompileError.InvalidArgumentCount(c.functionId, paramNum, c.paramNum)
                manager.addPending(Instruction.CallInstr(resolved))
            }
            is OpExpr -> TODO()
            is PassExpr -> TODO("remove this sometime")
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