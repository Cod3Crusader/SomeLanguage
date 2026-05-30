package com.archvin.pipeline.parsing

import com.archvin.exceptions.CompileError
import com.archvin.pipeline.Stage
import com.archvin.pipeline.lexing.SpecialToken
import com.archvin.pipeline.lexing.SpecialToken.*
import com.archvin.pipeline.lexing.Token
import com.archvin.pipeline.lexing.Token.IdentifierToken
import com.archvin.pipeline.parsing.Expression.*

class Parser : Stage<Expression, Token>() {
    private var callStack = ArrayDeque<CallExpr>()

    private var scopeStack = ArrayDeque<LambdaExpr>()

    override fun yield(add: Expression) {
        // scope logic
        scopeStack.lastOrNull()?.exprNum++

        // call logic
        callStack.lastOrNull()?.paramNum++

        if (callStack.isNotEmpty() && add !is CallExpr) {
            if (r.step() is CloseBracket) decCallDepth()
            else if (r.current() !is Comma) throw CompileError.UnexpectedError(",", r.current().raw)
        }

        // actually yield
        super.yield(add)
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

            OpenBraces -> {
                val expr = LambdaExpr()
                yield(expr)
                scopeStack.add(expr)
            }

            CloseBraces -> {
                if (scopeStack.isEmpty()) throw CompileError.UnexpectedError("expression", "}")
                scopeStack.removeLastOrNull()
            }

            else -> TODO("${t.raw} is not implemented in this context")
        }
    }

    private fun parseDeclaration(id: String, typeId: String) {
        if (r.step() is Assignment) {
            yield(DeclareExpr(id, typeId, true))
        }
        else if (r.current() is OpenBracket) {
            val paramTypes = arrayListOf<String>()

            if (r.peek() !is CloseBracket)
                while (true) {
                    val id = (r.step() as? IdentifierToken)?.id ?: throw CompileError.UnexpectedError("type identifier", r.current().raw)
                    paramTypes.add(id)

                    if (r.step() is CloseBracket) break
                    else if (r.current() !is Comma) throw CompileError.UnexpectedError(",", r.current().raw)
                }

            yield(DeclareExpr.FunDeclare(id, typeId, paramTypes))
        }
        else throw CompileError.UninitializedError(id)
    }

    private fun parseIdentifier(id: String) {
        when (val next = r.step()) {
            Assignment -> {
                yield(AssignExpr(id))
            }

            OpenBracket -> {
                val call = CallExpr(id)
                yield(call)
                callStack.add(call)
            }

            is IdentifierToken -> parseDeclaration(next.id, id)


            else -> {
                r.back()
                yield(ReadExpr(id))
            }
        }
    }

    override fun step(c: Token) {
        when (c) {
            is IdentifierToken -> parseIdentifier(c.id)
            is Token.LiteralToken<*> -> {
                yield(LitExpr(c.lit))
            }
            is SpecialToken -> parseSpecial(c)

            is Token.Test -> TODO("this shouldnt be here")
        }
    }
}