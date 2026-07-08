package com.archvin.pipeline.parsing

import com.archvin.exceptions.CompileError
import com.archvin.exceptions.CompileError.UnfinishedError
import com.archvin.pipeline.lexing.KeywordToken
import com.archvin.pipeline.lexing.KeywordToken.ReturnKw
import com.archvin.pipeline.lexing.SpecialToken
import com.archvin.pipeline.lexing.SpecialToken.*
import com.archvin.pipeline.lexing.Token
import com.archvin.pipeline.lexing.Token.IdentifierToken
import com.archvin.pipeline.parsing.AstNode.Declaration
import com.archvin.pipeline.parsing.AstNode.Declaration.FunDeclare
import com.archvin.pipeline.parsing.AstNode.Declaration.FunDeclare.Param
import com.archvin.pipeline.parsing.AstNode.Declaration.VarDeclare
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.reader.Reader
import com.archvin.utils.readAll
import com.archvin.utils.until

object Parser {
    private lateinit var r: Reader<Token>

    private fun parseKw(kw: KeywordToken) : Expression = when (kw) {
        ReturnKw -> ReturnExpr(nextExpr())
    }

    private fun parseFunctionDecl(id: String, typeId: String): FunDeclare {
        val params = ArrayList<Param>()

        while(r.step() != ClosePar) {
            val typeId = (r.current() as? IdentifierToken)?.id ?: throw CompileError.UnexpectedError("type identifier", r.current().raw)
            val paramId = (r.step() as? IdentifierToken)?.id ?: throw CompileError.UnexpectedError("type identifier", r.current().raw)

            params.add(Param(paramId, typeId))

            when (val got = r.step()) {
                is Comma -> {}
                is ClosePar -> break
                else -> throw CompileError.UnexpectedError(",", got.toString())
            }
        }

        val expr = mutableListOf<Expression>()
        val decl = mutableListOf<Declaration>()

        if (r.step() !is OpenBraces) throw CompileError.UninitializedError(id)
        r.until(CloseBraces) {
            val node = consume(it) ?: throw UnfinishedError("lambda expression")

            when (node) {
                is Expression -> expr.add(node)
                is Declaration -> decl.add(node)
            }
        }

        return FunDeclare(id, typeId, params, LambdaExpr(expr, decl))
    }

    private fun parseIdentifier(id: String): AstNode {
        return when (val next = r.step()) {
            Assignment -> AssignExpr(id, nextExpr() ?: throw UnfinishedError("assignment"))

            OpenPar -> {
                val params = ArrayList<Expression>()

                var expectComma = false

                r.until (ClosePar) {
                    if (expectComma && it !is Comma) throw CompileError.UnexpectedError(",", r.current().raw)
                    if (!expectComma) (consume(it) as? Expression)?.let { add -> params.add(add) } ?: throw UnfinishedError("call")

                    expectComma = !expectComma
                }

                CallExpr(id, params)
            }

            is IdentifierToken -> {
                val typeId = id
                val id = next.id

                if (r.step() is Assignment) {
                    VarDeclare(id, typeId, nextExpr() ?: error("Expected initialization"))
                } else if (r.current() is OpenPar) parseFunctionDecl(id, typeId)
                else throw CompileError.UninitializedError(id)
            }


            else -> {
                r.back()
                ReadExpr(id)
            }
        }
    }

    fun consume(c: Token): AstNode? {
        return when (c) {
            is IdentifierToken -> parseIdentifier(c.id)
            is Token.LiteralToken<*> -> LitExpr(c.lit)
            is KeywordToken -> parseKw(c)

            is SpecialToken -> throw CompileError.UnexpectedError("expression", c.raw)

            is Token.Test -> TODO("this shouldnt be here")
        }
    }

    fun nextExpr() = next() as? Expression
    fun next() = r.step()?.let { consume(it) }
    
    fun process(r: Reader<Token>): LambdaExpr {
        this.r = r

        val expr = ArrayList<Expression>()
        val decl = ArrayList<Declaration>()

        r.readAll {
            consume(it)?.let {
                when (it) {
                    is Expression -> expr.add(it)
                    is Declaration -> {
                        decl.add(it)
                        if (it is VarDeclare) expr.add(AssignExpr(it.id, it.init))
                    }
                }
            }
        }

        return LambdaExpr(expr, decl)
    }
}