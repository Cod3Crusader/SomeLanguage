package com.archvin.process

import com.archvin.exceptions.CompileError
import com.archvin.exceptions.RuntimeError
import com.archvin.instruction.Instruction
import com.archvin.reader.Reader
import com.archvin.token.LiteralToken
import com.archvin.token.OperatorToken
import com.archvin.token.Token
import com.archvin.type.BuiltinType
import com.archvin.type.HasId
import com.archvin.type.Type
import com.archvin.variable.FunctionValue
import com.archvin.variable.FunctionValue.BuiltinFunction.Println
import com.archvin.variable.Variable
import com.archvin.variable.Variable.Constant
import com.archvin.variable.Variable.Mutable
import java.util.*

class Parser : Processor<Instruction, Token>() {
    private val manager = InstructionManager()
    private val resolver = Resolver()

    private var callDepth = 0
    private var expectCloseBracket = 0

    private fun decCallDepth() {
        callDepth--
        expectCloseBracket++
    }

    private fun parseDeclaration(r: Reader<Token>, t: Type) {
        val nextToken = r.step()
        if (nextToken !is Token.Identifier) throw CompileError.UnexpectedError("identifier", nextToken.toString())

        val id = nextToken.id

        val variable = Mutable(id, t)
        resolver.add(variable)

        manager.addPending(Instruction.Assign(variable))

        if (r.step() != OperatorToken.Assignment) throw CompileError.UnexpectedError("=", r.current().raw)
    }

    private fun parseIdentifier(token: Token.Identifier, r: Reader<Token>) {
        val resolved = resolver.resolve(token.id) ?: throw RuntimeError.UnresolvedIdentifier(token.id)

        when (resolved) {
            is Type -> {
                parseDeclaration(r, resolved)
            }
            is Variable -> {
                when (r.step()) {
                    is OperatorToken.OpenBracket -> {
                        if (resolved.type is Type.FunctionType) {
                            callDepth++
                            manager.addPending(Instruction.Call(resolved.value as FunctionValue))
                        }
                        else throw CompileError.InvalidCallError(token)
                    }
                    is OperatorToken.Assignment -> manager.addPending(Instruction.Assign(resolved))
                    else -> {
                        manager.addPending(Instruction.Read(resolved))
                        r.back()
                    }
                }
            }
        }
    }

    override fun step(c: Token) {
        if (expectCloseBracket > 0) {
            if (c is OperatorToken.CloseBracket) expectCloseBracket--
            else throw CompileError.UnexpectedError("(", c.raw)

            return
        }

        when (c) {
            is Token.Identifier -> parseIdentifier(c, r)
            is LiteralToken<*> -> manager.addPending(Instruction.Literal(c))

            is OperatorToken -> {
                throw CompileError.UnexpectedError("expression", c.raw)
            }

            is Token.PassToken -> { /*TODO*/ }
            is Token.Test -> { /*TODO*/ }
        }
    }

    override fun post() {
        if (expectCloseBracket > 0) throw CompileError.UnexpectedError("(", "")
        if (callDepth > 0) throw CompileError.UnexpectedError("expression", "")
        if (manager.hasPending()) throw CompileError.UnexpectedError("expression", "")
    }

    private inner class InstructionManager {
        private inner class PendingInstruction(val instr: Instruction, var counter: Int)

        private val pending = Stack<PendingInstruction>()

        fun hasPending() = pending.isNotEmpty()

        private fun checkType(instr: Instruction) {
            if (hasPending()) {
                val top = pending.peek()
                val types = top.instr.paramTypes
                instr.assertType(types[types.size - top.counter])
            }
        }

        fun addPending(baseInstr: Instruction) {
            checkType(baseInstr)

            val counter = baseInstr.paramTypes.size

            if (counter == 0) {
                yield((baseInstr))
                tryPop()
            }
            else pending.push(PendingInstruction(baseInstr, counter))
        }

        private fun tryPop() {
            if (!hasPending()) return

            val instr = pending.pop().instr
            yield(instr)

            if (instr is Instruction.Call) decCallDepth()

            if (hasPending() && --pending.peek().counter <= 0) tryPop()
        }

    }

    private class Resolver {
        private val map = mutableMapOf<String, HasId>()

        init {
            add(BuiltinType.I32Type)
            add(BuiltinType.CharType)
            add(BuiltinType.StrType)
            add(BuiltinType.VoidType)

            add(Constant("println", Println))
        }

        fun add(value: HasId) {
            val id = value.id
            if (map.containsKey(id)) throw RuntimeError.Redeclaration(id)
            map[id] = value
        }

        fun resolve(id: String): HasId? = map[id]
        fun resolveType(id: String): Type? = map[id] as? Type
        fun resolveVar(id: String): Mutable? = map[id] as? Mutable
    }
}
