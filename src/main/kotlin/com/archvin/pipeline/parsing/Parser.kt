package com.archvin.pipeline.parsing

import com.archvin.exceptions.CompileError
import com.archvin.exceptions.CompileError.UnfinishedError
import com.archvin.pipeline.IStage
import com.archvin.pipeline.lexing.SpecialToken
import com.archvin.pipeline.lexing.SpecialToken.*
import com.archvin.pipeline.lexing.Token
import com.archvin.pipeline.lexing.Token.IdentifierToken
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.pipeline.parsing.Expression.Declaration.FunDeclare
import com.archvin.pipeline.parsing.Expression.Declaration.FunDeclare.Param
import com.archvin.pipeline.parsing.Expression.Declaration.VarDeclare

object Parser : IStage.ProvideConsume<Expression, Token>() {
    private fun parseFunctionDecl(id: String, typeId: String): FunDeclare {
        val params = ArrayList<Param>()

        while(r.step() != ClosePar) {
            val typeId = (r.current() as? IdentifierToken)?.id ?: throw CompileError.UnexpectedError("type identifier", r.current().raw)
            val paramId = (read() as? IdentifierToken)?.id ?: throw CompileError.UnexpectedError("type identifier", r.current().raw)

            params.add(Param(paramId, typeId))

            when (val got = read()) {
                is Comma -> {}
                is ClosePar -> break
                else -> throw CompileError.UnexpectedError(",", got.toString())
            }
        }

        val lambda = LambdaExpr()

        if (read() !is OpenBraces) throw CompileError.UninitializedError(id)
        until(CloseBraces) {
            val node = consume(it) ?: throw UnfinishedError("lambda expression")
            lambda.expressions.add(node)
        }

        return FunDeclare(id, typeId, params, lambda)
    }

    private fun parseIdentifier(id: String): Expression {
        return when (val next = r.step()) {
            Assignment -> AssignExpr(id, next() ?: throw UnfinishedError("assignment"))

            OpenPar -> {
                val params = ArrayList<Expression>()

                var expectComma = false

                until (ClosePar) {
                    if (expectComma && it !is Comma) throw CompileError.UnexpectedError(",", r.current().raw)
                    if (!expectComma) consume(it)?.let { add -> params.add(add) } ?: throw UnfinishedError("call")

                    expectComma = !expectComma
                }

                CallExpr(id, params)
            }

            is IdentifierToken -> {
                val typeId = id
                val id = next.id

                if (r.step() is Assignment) {
                    VarDeclare(id, typeId, next() ?: error("Expected initialization"))
                } else if (r.current() is OpenPar) parseFunctionDecl(id, typeId)
                else throw CompileError.UninitializedError(id)
            }


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

            is SpecialToken -> throw CompileError.UnexpectedError("expression", c.raw)

            is Token.Test -> TODO("this shouldnt be here")
        }
    }
}