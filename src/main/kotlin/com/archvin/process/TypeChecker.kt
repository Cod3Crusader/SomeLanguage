package com.archvin.process

import com.archvin.exceptions.CompileError
import com.archvin.expression.Expression
import com.archvin.expression.Expression.*
import com.archvin.instruction.Instruction
import com.archvin.reader.Reader
import com.archvin.type.BuiltinType
import com.archvin.type.HasId
import com.archvin.type.Type
import com.archvin.variable.FunctionValue
import com.archvin.variable.Variable

class TypeChecker : Processor<Instruction, Expression>() {
    private val resolver = Resolver()
    private val manager = InstructionManager()
    
    private fun parseIdentifier(id: String) {
        val resolved = resolver.resolve(id) ?: throw CompileError.UnresolvedIdentifier(id)

        when (resolved) {
            is Type -> {
                val next = r.step()
                
                if (next is AssignExpr) {
                    val variable = Variable.Mutable(next.variableId, resolved)
                    resolver.add(variable)
                    manager.addPending(Instruction.AssignInstr(variable))
                } else 
                    throw CompileError.UnexpectedError("expression", id)
            }
            is Variable -> {
                manager.addComplete(Instruction.ReadInstr(resolved))
            }
        }
    }

    override fun step(c: Expression) {
        when (c) {
            is IdExpr -> parseIdentifier(c.id)
            is LitExpr<*> -> manager.addComplete(Instruction.LitInstr(c.lit))
            is AssignExpr -> manager.addPending(Instruction.AssignInstr(resolver.resolveVar(c.variableId)))
            is CallExpr -> {
                val resolved = resolver.resolveFunc(c.functionId)
                val paramNum = resolved.paramTypes.size
                if (c.paramNum != paramNum) throw CompileError.InvalidArgumentCount(c.functionId, paramNum, c.paramNum)
                manager.addPending(Instruction.CallInstr(resolved))
            }
            is OpExpr -> {}
            is PassExpr -> TODO("remove this sometime")
        }
    }

    override fun process(r: Reader<Expression>): List<Instruction> {
        val ret = super.process(r)

        if (manager.hasPending()) throw CompileError.UnfinishedInstruction(manager.peek()!!.instr)

        return ret
    }

    internal class Resolver {
        private val map = mutableMapOf<String, HasId>()

        init {
            add(BuiltinType.I32Type)
            add(BuiltinType.CharType)
            add(BuiltinType.StrType)
            add(BuiltinType.VoidType)

            add(Variable.Constant("println", FunctionValue.BuiltinFunction.Println))
            add(Variable.Constant("add", FunctionValue.BuiltinFunction.Add))
        }

        fun add(value: HasId) {
            val id = value.id
            if (map.containsKey(id)) throw CompileError.Redeclaration(id)
            map[id] = value
        }

        fun resolve(id: String): HasId? = map[id]
        fun resolveType(id: String) = map[id] as? Type ?: throw CompileError.UnresolvedIdentifier(id)
        fun resolveFunc(id: String) = (map[id] as? Variable)?.value as? FunctionValue ?: throw CompileError.InvalidCallError(id)
        fun resolveVar(id: String) = map[id] as? Variable ?: throw CompileError.UnresolvedIdentifier(id)

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