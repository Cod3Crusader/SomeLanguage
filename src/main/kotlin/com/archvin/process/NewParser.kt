package com.archvin.process

import com.archvin.exceptions.CompileError
import com.archvin.expression.Expression
import com.archvin.expression.Expression.IdExpr
import com.archvin.expression.Expression.LitExpr
import com.archvin.token.SpecialToken
import com.archvin.token.SpecialToken.*
import com.archvin.token.Token

class NewParser : Processor<Expression, Token>() {
    private var prev: Token.Identifier? = null

    private var callStack = ArrayDeque<Expression.CallExpr>()

    private fun consumePrev(consumer: (String) -> Unit) {
        if (prev == null) throw CompileError.UnexpectedError("expression", r.current().raw)
        consumer(prev!!.id)
        prev = null
    }

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
            Assignment -> consumePrev {
                yield(Expression.AssignExpr(it))
            }

            OpenBracket -> consumePrev {
                val call = Expression.CallExpr(it)
                callStack.add(call)
                yield(call)
            }

            CloseBracket -> decCallDepth()

            Comma -> {
                if (callStack.isEmpty()) throw CompileError.UnexpectedError("expression", ",")
                consumePrev {  super.yield(IdExpr(it)) }
            }

            else -> TODO()
        }
    }

    override fun step(c: Token) {
        when (c) {
            is Token.Identifier -> {
                prev?.let { yield(IdExpr(it.id)) }
                prev = c
            }
            is Token.LiteralToken<*> -> {
                if (prev != null)  yield(IdExpr(prev!!.id))
                yield(LitExpr(c.lit))
            }
            is SpecialToken -> parseSpecial(c)

            is Token.Test -> TODO("this shouldnt be here")
        }
    }
}