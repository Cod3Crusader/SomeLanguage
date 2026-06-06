package com.archvin.pipeline.parsing

import com.archvin.exceptions.CompileError
import com.archvin.exceptions.CompileError.UnfinishedError
import com.archvin.pipeline.Stage
import com.archvin.pipeline.lexing.SpecialToken
import com.archvin.pipeline.lexing.SpecialToken.*
import com.archvin.pipeline.lexing.Token
import com.archvin.pipeline.lexing.Token.IdentifierToken
import com.archvin.pipeline.parsing.Expression.*

class Parser : Stage.ConsumerStage<Expression, Token>() {
    private fun parseSpecial(t: SpecialToken): Expression {
        // TODO: consider removal
        return when (t) {
            OpenBraces -> {
                val expressions = ArrayList<Expression>()

                while (r.step() !is CloseBraces) {
                    expressions.add(consume(r.current()) ?: throw UnfinishedError("lambda expression"))
                }

                LambdaExpr(expressions)
            }

            else -> throw CompileError.UnexpectedError("expression", t.raw)
        }
    }

    private fun parseDeclaration(id: String, typeId: String): DeclareExpr {
        return if (r.step() is Assignment) DeclareExpr(id, typeId, true)
        else if (r.current() is OpenPar) {
            val paramTypes = arrayListOf<String>()


            while (r.step() !is ClosePar) {
                val id = (r.current() as? IdentifierToken)?.id ?: throw CompileError.UnexpectedError("type identifier", r.current().raw)
                paramTypes.add(id)

                when (r.step()) {
                    is ClosePar -> break
                    is Comma -> {}
                    !is Comma -> throw CompileError.UnexpectedError(",", r.current().raw)
                }
            }

            DeclareExpr.FunDeclare(id, typeId, paramTypes)
        }
        else throw CompileError.UninitializedError(id)
    }

    private fun parseIdentifier(id: String): Expression {
        return when (val next = r.step()) {
            Assignment -> AssignExpr(id, next() ?: throw UnfinishedError("assignment"))

            OpenPar -> {
                val params = ArrayList<Expression>()

                while (r.step() != ClosePar) {
                    params.add(consume(r.current()) ?: throw UnfinishedError("call"))


                    when (r.step()) {
                        is ClosePar -> break
                        is Comma -> {}
                        !is Comma -> throw CompileError.UnexpectedError("expression", ",")
                    }
                }

                CallExpr(id, params)
            }

            is IdentifierToken -> parseDeclaration(next.id, id)


            else -> {
                r.back()
                ReadExpr(id)
            }
        }
    }

    override fun consume(c: Token): Expression? {
        return when (c) {
            is IdentifierToken -> parseIdentifier(c.id)
            is Token.LiteralToken<*> -> LitExpr(c.lit)

            is SpecialToken -> parseSpecial(c)

            is Token.Test -> TODO("this shouldnt be here")
        }
    }
}