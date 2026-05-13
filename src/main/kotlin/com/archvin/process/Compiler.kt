package com.archvin.process

import com.archvin.exceptions.CompileError
import com.archvin.exceptions.RuntimeError
import com.archvin.instruction.Instruction
import com.archvin.reader.Reader
import com.archvin.token.LiteralToken
import com.archvin.token.OperatorToken
import com.archvin.token.Token
import com.archvin.type.Type
import com.archvin.variable.Variable
import java.util.*

class Compiler(val resolver: Resolver) : Processor<Instruction, Token>() {
    private val manager = this.Manager()

    fun parseDeclaration(r: Reader<Token>, t: Type) {
        val nextToken = r.step()
        if (nextToken !is Token.Identifier) throw CompileError.UnexpectedError("identifier", nextToken.toString())

        val id = nextToken.id

        val variable = Variable(id, t)
        resolver.add(variable)

        manager.add(Instruction.Declare(id, t))
    }

    fun parseIdentifier(token: Token.Identifier, r: Reader<Token>) {
        if (token.id == "debug") {
            manager.add(Instruction.Debug)
            return
        } // TODO: replace

        val resolved = resolver.resolve(token.id) ?: throw RuntimeError.UnresolvedIdentifier(token.id)

        when (resolved) {
            is Type -> {
                parseDeclaration(r, resolved)
            }
            is Variable -> manager.add(Instruction.Read(resolved))
        }
    }

    override fun step(c: Token) {
        when (c) {
            is Token.Identifier -> parseIdentifier(c, r)
            is LiteralToken<*> -> manager.add(Instruction.Literal(c))

            is OperatorToken -> { /*TODO*/ }

            is Token.NullToken -> { /*TODO*/ }
            is Token.Test -> { /*TODO*/ }
        }
    }

    private data class PendingInstr(val instr: Instruction, var counter: Int)
    private inner class Manager {
        private val pending = Stack<PendingInstr>()

        fun hasPending() = pending.isNotEmpty()

        fun add(baseInstr: Instruction) {
            if (hasPending()) {
                val top = pending.peek()
                val types = top.instr.paramTypes
                baseInstr.asserType(types[types.size - top.counter])
            }

            val counter = baseInstr.paramTypes.size
            pending.push(PendingInstr(baseInstr, counter))

            if (counter == 0) pop()
        }

        fun pop() {
            val instr = pending.pop().instr
            ret.add(instr)

            if (hasPending() && --pending.peek().counter <= 0) pop()
        }

    }
}