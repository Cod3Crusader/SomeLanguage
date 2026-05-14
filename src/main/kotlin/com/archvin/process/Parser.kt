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
import com.archvin.variable.Variable
import java.util.*

class Parser : Processor<Instruction, Token>() {
    private val manager = InstructionManager()
    private val resolver = Resolver()

    private var expectToken = Token.PassToken

    fun parseDeclaration(r: Reader<Token>, t: Type) {
        val nextToken = r.step()
        if (nextToken !is Token.Identifier) throw CompileError.UnexpectedError("identifier", nextToken.toString())

        val id = nextToken.id

        val variable = Variable(id, t)
        resolver.add(variable)

        manager.add(Instruction.Assign(variable))

        if (r.step() != OperatorToken.Assignment) throw CompileError.UnexpectedError("=", r.current().raw)
    }

    fun parseIdentifier(token: Token.Identifier, r: Reader<Token>) {
        if (token.id == "println") {
            manager.add(Instruction.Println)
            return
        } // TODO: replace

        val resolved = resolver.resolve(token.id) ?: throw RuntimeError.UnresolvedIdentifier(token.id)

        when (resolved) {
            is Type -> {
                parseDeclaration(r, resolved)
            }
            is Variable -> {
                if (r.peek() == OperatorToken.Assignment) {
                    manager.add(Instruction.Assign(resolved))
                    r.step()
                }
                else manager.add(Instruction.Read(resolved))
            }
        }
    }

    override fun step(c: Token) {
        when (c) {
            is Token.Identifier -> parseIdentifier(c, r)
            is LiteralToken<*> -> manager.add(Instruction.Literal(c))

            is OperatorToken -> {
                throw CompileError.UnexpectedError("expression", c.raw)
            }

            is Token.PassToken -> { /*TODO*/ }
            is Token.Test -> { /*TODO*/ }
        }
    }

    private inner class InstructionManager {
        private inner class PendingInstruction(val instr: Instruction, var counter: Int)

        private val pending = Stack<PendingInstruction>()

        fun hasPending() = pending.isNotEmpty()

        fun add(baseInstr: Instruction) {
            if (hasPending()) {
                val top = pending.peek()
                val types = top.instr.paramTypes
                baseInstr.assertType(types[types.size - top.counter])
            }

            val counter = baseInstr.paramTypes.size
            pending.push(PendingInstruction(baseInstr, counter))

            if (counter == 0) pop()
        }

        fun pop() {
            val instr = pending.pop().instr
            yield(instr)

            if (hasPending() && --pending.peek().counter <= 0) pop()
        }

    }

    private class Resolver {
        private val map = mutableMapOf<String, HasId>()

        init {
            add(BuiltinType.I32Type)
            add(BuiltinType.CharType)
            add(BuiltinType.StrType)
            add(BuiltinType.VoidType)
        }

        fun add(value: HasId) {
            val id = value.id
            if (map.containsKey(id)) throw RuntimeError.Redeclaration(id)
            map[id] = value
        }

        fun resolve(id: String): HasId? = map[id]
        fun resolveType(id: String): Type? = map[id] as? Type
        fun resolveVar(id: String): Variable? = map[id] as? Variable
    }
}
