package com.archvin.pipeline.parsing

import com.archvin.exceptions.CompileError
import com.archvin.pipeline.Stage
import com.archvin.pipeline.lexing.SpecialToken
import com.archvin.pipeline.lexing.Token
import com.archvin.pipeline.lexing.Token.IdentifierToken

class Parser : Stage<Expression, Token>() {
    private var callStack = ArrayDeque<Expression.CallExpr>()

    override fun yield(add: Expression) {
        callStack.lastOrNull()?.paramNum++

        if (callStack.isNotEmpty() && add !is Expression.CallExpr) {
            if (r.step() is SpecialToken.CloseBracket) decCallDepth()
            else if (r.current() !is SpecialToken.Comma) throw CompileError.UnexpectedError(",", r.current().raw)
        }

        super.yield(add)
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

    private fun parseDeclaration(id: String, typeId: String) {
        if (r.step() is SpecialToken.Assignment) {
            yield(Expression.DeclareExpr(id, typeId, true))
        }
        /*
        else if (r.current() is SpecialToken.OpenBracket) {
            val paramTypes = arrayListOf<String>()
            while (true) {
                val id = (r.step() as? IdentifierToken)?.id ?: throw CompileError.UnexpectedError("type identifier", r.current().raw)
                paramTypes.add(id)

                if (r.step() is SpecialToken.CloseBracket) break
                else if (r.current() !is SpecialToken.Comma) throw CompileError.UnexpectedError(",", r.current().raw)
            }
            header.elements.add(HeaderElement.FunctionDeclaration(id, typeId, paramTypes))
        }
        */
        else throw CompileError.UninitializedError(id)
    }

    private fun parseIdentifier(id: String) {
        when (val next = r.step()) {
            SpecialToken.Assignment -> {
                yield(Expression.AssignExpr(id))
            }

            SpecialToken.OpenBracket -> {
                val call = Expression.CallExpr(id)
                yield(call)
                callStack.add(call)
            }

            is IdentifierToken -> parseDeclaration(next.id, id)


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