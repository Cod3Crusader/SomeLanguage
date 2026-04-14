package com.archvin.process

import com.archvin.Resolver
import com.archvin.exceptions.RuntimeError
import com.archvin.expression.Expression
import com.archvin.reader.ProcessorInputReader
import com.archvin.token.LiteralToken
import com.archvin.token.OperatorToken
import com.archvin.token.Token
import com.archvin.type.Type
import kotlin.reflect.KClass

class Expressionizer(r: ProcessorInputReader<Token>) : Processor<Token, Unit>(r) {
    lateinit var expressions: Array<Expression>

    private var current: KClass<out Expression> = Expression.PassExpression::class
    private var parts = mutableListOf<Token>()
    private var expects: Expectation = Expectation.None

    fun parseIdentifier(token: Token.IdentifierToken) {
        val resolved = Resolver.resolve(token.id) ?: throw RuntimeError.UnresolvedIdentifier(token.id)

        when (resolved) {
            is Type -> {

            }
        }
    }

    override fun process(): Array<Unit> {
        r.reset()


        while (!r.isEof()) {
            when (val token = r.current()) {
                is Token.IdentifierToken -> parseIdentifier(token)
                is OperatorToken -> {TODO()}
                is LiteralToken<*> -> {TODO()}

                is Token.NullToken -> {}
                is Token.TestToken -> {}
            }

            r.step()
        }

        return emptyArray()
    }

    private enum class Expectation(val token: Token?, val clazz: KClass<*>?, val type: Type?) {
        None(null, null, null),
    }
}