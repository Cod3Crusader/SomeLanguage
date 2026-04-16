package com.archvin.process

import com.archvin.Resolver
import com.archvin.exceptions.CompileError
import com.archvin.exceptions.RuntimeError
import com.archvin.expression.Expression
import com.archvin.reader.Reader
import com.archvin.token.LiteralToken
import com.archvin.token.OperatorToken
import com.archvin.token.Token
import com.archvin.type.HasType
import com.archvin.type.Type
import kotlin.reflect.KClass

object Expressionizer : Processor<Expression, Token> {
    private var current: KClass<out Expression> = Expression.PassExpression::class
    private var parts = mutableListOf<Token>()
    private var expects: Expectation = Expectation.None

    fun parseIdentifier(token: Token.IdentifierToken, r: Reader<Token>) {
        if (token.id == "println") {
            // TODO
            val next = r.step() as? LiteralToken<*> ?: throw CompileError.UnexpectedError("literal", r.current().toString())
            println(next.value)
        }


        val resolved = Resolver.resolve(token.id) ?: throw RuntimeError.UnresolvedIdentifier(token.id)

        when (resolved) {
            is Type -> {

            }
        }
    }

    override fun step(c: Token, r: Reader<Token>): Expression? {
        when (c) {
            is Token.IdentifierToken -> parseIdentifier(c, r)
            is OperatorToken -> { /*TODO*/ }

            is LiteralToken<*> -> { /*TODO*/ }

            is Token.NullToken -> { /*TODO*/ }
            is Token.TestToken -> { /*TODO*/ }
        }

        return null
    }

    private enum class Expectation(val token: Token?, val clazz: KClass<*>?, val type: Type?) {
        None(null, null, null),
    }
}