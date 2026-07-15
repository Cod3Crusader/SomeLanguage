package com.archvin.pipeline.parsing

import com.archvin.data.Literal.BoolLiteral
import com.archvin.exceptions.CompileError
import com.archvin.exceptions.CompileError.UnfinishedError
import com.archvin.pipeline.lexing.KeywordToken
import com.archvin.pipeline.lexing.KeywordToken.*
import com.archvin.pipeline.lexing.SpecialToken
import com.archvin.pipeline.lexing.SpecialToken.*
import com.archvin.pipeline.lexing.Token
import com.archvin.pipeline.lexing.Token.IdentifierToken
import com.archvin.pipeline.parsing.AstNode.Declaration
import com.archvin.pipeline.parsing.AstNode.Declaration.*
import com.archvin.pipeline.parsing.AstNode.Declaration.UncheckedType.TypeId
import com.archvin.pipeline.parsing.Expression.*
import com.archvin.reader.Reader
import com.archvin.utils.readAll
import com.archvin.utils.until

object Parser {
    private lateinit var r: Reader<Token>

    private fun processLambdaExpr() : LambdaExpr {
        val expr = ArrayList<Expression>()
        val decl = ArrayList<Declaration>()

        r.until(CloseBraces) {
            val node = consume(it) ?: throw UnfinishedError("lambda expression")

            when (node) {
                is Expression -> expr.add(node)
                is Declaration -> {
                    decl.add(node)
                    if (node is VarDeclare && node.init != null) expr.add(AssignExpr(node.id, node.init))
                }
            }
        }

        return LambdaExpr(expr, decl)
    }

    private fun parseDeclaration(id: String, type: UncheckedType) =
        if (r.step() is Assignment) {
            VarDeclare(id, type, nextExpr() ?: error("Expected initialization"))
        } else if (r.current() is OpenPar) parseFunctionDecl(id, type)
        else throw CompileError.UninitializedError(id)


    private fun parseLambdaType(): UncheckedType.LambdaType {
        // assumes LambdaKw is consumed

        val retType = nextType() ?: throw CompileError.ExpectationError("return type", r.current().raw)

        if (r.step() != OpenPar) throw CompileError.ExpectationError("(", r.current().raw)
        val paramTypes = ArrayList<UncheckedType>()

        var expectComma = false

        r.until (ClosePar) {
            if (expectComma && it !is Comma) throw CompileError.ExpectationError(",", r.current().raw)
            if (!expectComma) (nextType())?.let { add -> paramTypes.add(add) } ?: throw UnfinishedError("lambda parameter type")

            expectComma = !expectComma
        }

        return UncheckedType.LambdaType(retType, paramTypes)
    }

    private fun parseKw(kw: KeywordToken) : AstNode = when (kw) {
        ReturnKw -> ReturnExpr(nextExpr())
        TrueKw -> LitExpr(BoolLiteral(true))
        FalseKw -> LitExpr(BoolLiteral(false))

        IfKw -> {
            // check for syntax (parenthesis)
            if (r.step() != OpenPar) throw CompileError.ExpectationError("(", r.current().raw)

            val condition = nextExpr() ?: throw UnfinishedError("if condition")

            if (r.step() != ClosePar) throw CompileError.ExpectationError(")", r.current().raw)

            val body = nextExpr() ?: throw UnfinishedError("if body")
            val elseBranch = if (r.peek() == ElseKw) {
                r.step()
                nextExpr() ?: throw UnfinishedError("else branch")
            } else null

            ConditionalExpr(condition, body, elseBranch)
        }

        LambdaKw -> {
            val type = parseLambdaType()
            val id = r.step() as? IdentifierToken ?: throw CompileError.ExpectationError("identifier", r.current().raw)
            parseDeclaration(id.raw, type)
        }

        else -> throw CompileError.ExpectationError("expression", kw.raw)
    }

    private fun parseFunctionDecl(id: String, type: UncheckedType): FunDeclare {
        val params = ArrayList<VarDeclare>()

        if (r.peek() != ClosePar)
            while (true) {
                val type = nextType() ?: throw CompileError.ExpectationError("type identifier", r.current().raw)
                val paramId = (r.step() as? IdentifierToken)?.id ?: throw CompileError.ExpectationError(
                    "type identifier",
                    r.current().raw
                )

                params.add(VarDeclare(paramId, type))

                when (val got = r.step()) {
                    is Comma -> {}
                    is ClosePar -> break // the loop will detect ClosePar and break
                    else -> throw CompileError.ExpectationError(",", got.toString())
                }
            }
        else r.step()


        val lambda = nextLambda() ?: throw CompileError.UninitializedError(id)

        return FunDeclare(id, type, params, lambda)
    }

    private fun parseIdentifier(id: String): AstNode {
        return when (val next = r.step()) {
            Assignment -> AssignExpr(id, nextExpr() ?: throw UnfinishedError("assignment"))

            OpenPar -> {
                val params = ArrayList<Expression>()

                var expectComma = false

                r.until (ClosePar) {
                    if (expectComma && it !is Comma) throw CompileError.ExpectationError(",", r.current().raw)
                    if (!expectComma) (consume(it) as? Expression)?.let { add -> params.add(add) } ?: throw UnfinishedError("call")

                    expectComma = !expectComma
                }

                CallExpr(id, params)
            }

            is IdentifierToken -> {
                val typeId = id
                val id = next.id

                parseDeclaration(id, TypeId(typeId))
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

            is SpecialToken -> {
                if (c is OpenBraces) processLambdaExpr()
                else throw CompileError.UnexpectedError("character ${c.raw}")
            }

            is Token.Test -> TODO("this shouldnt be here")
        }
    }

    private fun nextType(): UncheckedType? {
        return when (val base = r.step()) {
            is IdentifierToken -> TypeId(base.id)
            is LambdaKw -> parseLambdaType()
            else -> null
        }
    }
    private fun nextLambda(): LambdaExpr? {
        if (r.peek() !is OpenBraces) return null
        r.step()
        return processLambdaExpr()
    }
    private fun nextExpr() = next() as? Expression
    private fun next() = r.step()?.let { consume(it) }
    
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
                        if (it is VarDeclare && it.init != null) expr.add(AssignExpr(it.id, it.init))
                    }
                }
            }
        }

        return LambdaExpr(expr, decl)
    }
}