package com.archvin.pipeline.parsing

import com.archvin.exceptions.CompileError
import com.archvin.exceptions.CompileError.UnfinishedError
import com.archvin.pipeline.IStage
import com.archvin.pipeline.lexing.SpecialToken
import com.archvin.pipeline.lexing.SpecialToken.*
import com.archvin.pipeline.lexing.Token
import com.archvin.pipeline.lexing.Token.IdentifierToken
import com.archvin.pipeline.parsing.AstNode.Declaration
import com.archvin.pipeline.parsing.AstNode.Declaration.FunDeclare
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.reader.Reader

object Parser : IStage.IConsumer<AstNode, Scope, Token> {
    override lateinit var r: Reader<Token>
    val topScope = Scope()

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

        val expressions = ArrayList<Expression>()
        val scope = Scope()

        until(CloseBraces) {
            val node = consume(r.current()) ?: throw UnfinishedError("lambda expression")
            when (node) {
                is Declaration -> scope.add(node)
                is Expression-> expressions.add(node)
            }
        }

        return FunDeclare(id, typeId, paramTypes, scope, expressions)
    }

    private fun parseIdentifier(id: String): AstNode {
        return when (val next = r.step()) {
            Assignment -> AssignExpr(id, next() as? Expression ?: throw UnfinishedError("assignment"))

            OpenPar -> {
                val params = ArrayList<Expression>()

                until(ClosePar) {
                    params.add(consume(r.current()) as? Expression ?: throw UnfinishedError("call"))

                    when (r.step()) {
                        is Comma -> {}
                        !is Comma -> throw CompileError.UnexpectedError("expression", ",")
                    }
                }

                CallExpr(id, params)
            }

            is IdentifierToken ->
                if (r.step() is Assignment) Declaration(next.id, id, true)
                else if (r.current() is OpenPar) parseFunction(next.id, id)
                else throw CompileError.UninitializedError(next.id)


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

    override fun ret() = topScope
}