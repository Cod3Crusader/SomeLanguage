package com.archvin.process

import com.archvin.exceptions.CompileError
import com.archvin.expression.Expression
import com.archvin.expression.Expression.LitExpr
import com.archvin.token.SpecialToken
import com.archvin.token.SpecialToken.*
import com.archvin.token.Token

class Parser : Processor<Expression, Token>() {
    private var callStack = ArrayDeque<Expression.CallExpr>()

    override fun yield(add: Expression) {
        super.yield(add)

        callStack.lastOrNull()?.paramNum++

        if (callStack.isNotEmpty() && add !is Expression.CallExpr) {
            if (r.step() is Comma) callStack.last().paramNum++
            else if (r.current() is CloseBracket) decCallDepth()
            else throw CompileError.UnexpectedError(",", r.current().raw)
        }
    }

    fun decCallDepth() {
        callStack.removeLastOrNull()
        if (callStack.isNotEmpty()) {
            if (r.step() is CloseBracket) decCallDepth()
            else if (r.current() !is Comma) throw CompileError.UnexpectedError(",", r.current().raw)
        }
    }

    private fun parseSpecial(t: SpecialToken) {
        when (t) {
            CloseBracket -> decCallDepth()

            Comma -> {
                if (callStack.isEmpty()) throw CompileError.UnexpectedError("expression", ",")
            }

            else -> TODO()
        }
    }

    private fun parseIdentifier(id: String) {
        when (r.step()) {
            Assignment -> {
                yield(Expression.AssignExpr(id))
            }

            OpenBracket -> {
                val call = Expression.CallExpr(id)
                callStack.add(call)
                yield(call)
            }

            else -> {
                r.back()
                yield(Expression.ReadExpr(id))
            }
        }

    }

    override fun step(c: Token) {
        when (c) {
            is Token.Identifier -> parseIdentifier(c.id)
            is Token.LiteralToken<*> -> {
                yield(LitExpr(c.lit))
            }
            is SpecialToken -> parseSpecial(c)

            is Token.Test -> TODO("this shouldnt be here")
        }
    }
}