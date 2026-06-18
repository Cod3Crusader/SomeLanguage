package com.archvin.pipeline.parsing

import com.archvin.exceptions.CompileError
import com.archvin.exceptions.CompileError.UnfinishedError
import com.archvin.pipeline.IStage
import com.archvin.pipeline.lexing.SpecialToken
import com.archvin.pipeline.lexing.SpecialToken.*
import com.archvin.pipeline.lexing.Token
import com.archvin.pipeline.lexing.Token.IdentifierToken
import com.archvin.pipeline.parsing.AstNode.Declaration.FunDeclare
import com.archvin.pipeline.parsing.AstNode.Declaration.VarDeclare
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.reader.Reader

object Parser : IStage.IConsumer<AstNode, LambdaExpr, Token> {
    override lateinit var r: Reader<Token>
    val topLambda = LambdaExpr()

    private fun parseFunction(id: String, typeId: String): FunDeclare {
        val paramTypes = arrayListOf<String>()

        until(ClosePar) {
            val id = (r.current() as? IdentifierToken)?.id ?: throw CompileError.UnexpectedError("type identifier", r.current().raw)
            paramTypes.add(id)

            when (r.step()) {
                is Comma -> {}
                !is Comma -> throw CompileError.UnexpectedError(",", r.current().raw)
            }
        }

        val lambda = LambdaExpr()

        if (read() !is OpenBraces) throw CompileError.UninitializedError(id)
        until(CloseBraces) {
            val node = consume(it) ?: throw UnfinishedError("lambda expression")
            lambda.add(node)
        }

        return FunDeclare(id, typeId, paramTypes, lambda)
    }

    private fun parseIdentifier(id: String): AstNode {
        return when (val next = r.step()) {
            Assignment -> AssignExpr(id, next() as? Expression ?: throw UnfinishedError("assignment"))

            OpenPar -> {
                val params = ArrayList<Expression>()

                var expectComma = false

                until (ClosePar) {
                    if (expectComma && it !is Comma) throw CompileError.UnexpectedError(",", r.peek().raw)
                    if (!expectComma) (consume(it) as? Expression) ?.let { add -> params.add(add) } ?: throw UnfinishedError("call")

                    expectComma = !expectComma
                }

                CallExpr(id, params)
            }

            is IdentifierToken -> {
                val typeId = id
                val id = next.id

                if (r.step() is Assignment) {
                    val init = AssignExpr(id, next() as? Expression ?: error("Expected initialization"))
                    VarDeclare(id, typeId, init)
                } else if (r.current() is OpenPar) parseFunction(id, typeId)
                else throw CompileError.UninitializedError(id)
            }


            else -> {
                r.back()
                ReadExpr(id)
            }
        }
    }
    
    override fun consume(c: Token): AstNode? {
        return when (c) {
            is IdentifierToken -> parseIdentifier(c.id)
            is Token.LiteralToken<*> -> LitExpr(c.lit)

            is SpecialToken -> throw CompileError.UnexpectedError("expression", c.raw)

            is Token.Test -> TODO("this shouldnt be here")
        }
    }

    override fun step(c: Token) { consume(c)?.let { topLambda.add(it) } }
    override fun ret() = topLambda
}