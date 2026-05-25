package com.archvin.parser

import com.archvin.Processor
import com.archvin.exceptions.CompileError
import com.archvin.program.Instruction
import com.archvin.reader.Reader
import com.archvin.token.SpecialToken
import com.archvin.token.Token
import com.archvin.type.BuiltinType
import com.archvin.type.HasId
import com.archvin.type.Type
import com.archvin.variable.FunctionValue
import com.archvin.variable.Variable
import java.util.*

@Deprecated("Now split into a parser and type checker", replaceWith = ReplaceWith("Parser", "com/archvin/process/Parser.kt"))
class OldParser : Processor<Instruction, Token>() {
    private val manager = InstructionManager()
    private val resolver = Resolver()

    private var callDepth = 0
        set(value) { field = value; expectComma = false }
    private var expectCloseBracket = 0
    private var expectComma = false

    private fun decCallDepth() {
        callDepth--
        expectCloseBracket++
    }

    private fun parseDeclaration(r: Reader<Token>, t: Type) {
        val nextToken = r.step()
        if (nextToken !is Token.IdentifierToken) throw CompileError.UnexpectedError("identifier", nextToken.toString())

        val id = nextToken.id

        val variable = Variable.Mutable(id, t)
        resolver.add(variable)

        manager.addPending(Instruction.AssignInstr(variable))

        if (r.step() != SpecialToken.Assignment) throw CompileError.UnexpectedError("=", r.current().raw)
    }

    private fun parseCall(callable: Variable) {
        if (callable.type is Type.FunctionType) {
            callDepth++
            manager.addPending(Instruction.CallInstr(callable.value as FunctionValue))
        }
        else throw CompileError.InvalidCallError(callable.id)
    }

    private fun parseIdentifier(token: Token.IdentifierToken, r: Reader<Token>) {
        val resolved = resolver.resolve(token.id) ?: throw CompileError.UnresolvedIdentifier(token.id)

        when (resolved) {
            is Type -> {
                parseDeclaration(r, resolved)
            }
            is Variable -> {
                when (r.step()) {
                    is SpecialToken.OpenBracket -> parseCall(resolved)
                    is SpecialToken.Assignment -> manager.addPending(Instruction.AssignInstr(resolved))
                    else -> {
                        manager.addPending(Instruction.ReadInstr(resolved))
                        r.back()
                    }
                }
            }
        }
    }

    override fun step(c: Token) {
        // TODO: figure out a less ugly way to do this
        if (expectCloseBracket > 0) {
            if (c is SpecialToken.CloseBracket) expectCloseBracket--
            else throw CompileError.UnexpectedError(")", c.raw)

            return
        }
        if (callDepth > 0) {
            if (expectComma) {
                if (c !is SpecialToken.Comma) throw CompileError.UnexpectedError(",", c.raw)
                expectComma = false
                return
            }
            expectComma = true
        }

        when (c) {
            is Token.IdentifierToken -> parseIdentifier(c, r)
            is Token.LiteralToken<*> -> manager.addPending(Instruction.LitInstr(c.lit))

            is SpecialToken -> {
                throw CompileError.UnexpectedError("expression", c.raw)
            }

            is Token.Test -> TODO()
        }
    }

    override fun process(r: Reader<Token>): List<Instruction> {
        val ret = super.process(r)

        if (expectCloseBracket > 0) throw CompileError.UnexpectedError(")", "")
        if (callDepth > 0) throw CompileError.UnexpectedError("expression", "")
        if (manager.hasPending()) throw CompileError.UnexpectedError("expression", "")

        return ret
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
            pending.push(PendingInstruction(baseInstr, counter))

            if (counter == 0) tryPop()
        }

        private fun tryPop() {
            if (!hasPending()) return

            val instr = pending.pop().instr
            yield(instr)

            if (instr is Instruction.CallInstr) decCallDepth()

            if (hasPending() && --pending.peek().counter <= 0) tryPop()
        }
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
        fun resolveType(id: String): Type? = map[id] as? Type
        fun resolveVar(id: String): Variable.Mutable? = map[id] as? Variable.Mutable
    }
}