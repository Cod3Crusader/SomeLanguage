package com.archvin.parser

import com.archvin.exceptions.CompileError
import com.archvin.expression.Expression
import com.archvin.process.Processor
import com.archvin.token.SpecialToken
import com.archvin.token.Token
import com.archvin.token.Token.IdentifierToken
import com.archvin.type.Type
import com.archvin.variable.Variable

class Parser(private val resolver: Resolver) : Processor<Expression, Token>() {
    private var callStack = ArrayDeque<Expression.CallExpr>()

    override fun yield(add: Expression) {
        super.yield(add)

        callStack.lastOrNull()?.paramNum++

        if (callStack.isNotEmpty() && add !is Expression.CallExpr) {
            if (r.step() is SpecialToken.Comma) callStack.last().paramNum++
            else if (r.current() is SpecialToken.CloseBracket) decCallDepth()
            else throw CompileError.UnexpectedError(",", r.current().raw)
        }
    }

    fun decCallDepth() {
        callStack.removeLastOrNull()
        if (callStack.isNotEmpty()) {
            if (r.step() is SpecialToken.CloseBracket) decCallDepth()
            else if (r.current() !is SpecialToken.Comma) throw CompileError.UnexpectedError(",", r.current().raw)
        }
    }

    private fun parseSpecial(t: SpecialToken) {
        when (t) {
            SpecialToken.CloseBracket -> decCallDepth()

            SpecialToken.Comma -> {
                if (callStack.isEmpty()) throw CompileError.UnexpectedError("expression", ",")
            }

            else -> TODO("${t.raw} is not implemented in this context")
        }
    }

    private fun parseIdentifier(id: String) {
        val resolved = resolver.resolve(id)

        when (val next = r.step()) {
            SpecialToken.Assignment -> {
                yield(Expression.AssignExpr(id))
            }

            SpecialToken.OpenBracket -> {
                val call = Expression.CallExpr(id)
                callStack.add(call)
                yield(call)
            }

            is IdentifierToken -> {
                if (resolved is Type) {
                    val variable = Variable.Mutable(next.id, resolved)
                    resolver.add(variable)
                    yield(Expression.AssignExpr(variable.id))

                    if (r.step() !is SpecialToken.Assignment) throw CompileError.UninitializedError(variable)
                } else throw CompileError.UnexpectedError("expression", id)
            }

            else -> {
                r.back()
                yield(Expression.ReadExpr(id))
            }
        }
    }

    override fun step(c: Token) {
        when (c) {
            is IdentifierToken -> parseIdentifier(c.id)
            is Token.LiteralToken<*> -> {
                yield(Expression.LitExpr(c.lit))
            }
            is SpecialToken -> parseSpecial(c)

            is Token.Test -> TODO("this shouldnt be here")
        }
    }
}