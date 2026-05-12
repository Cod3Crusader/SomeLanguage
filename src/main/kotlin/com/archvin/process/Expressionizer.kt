package com.archvin.process

import com.archvin.exceptions.CompileError
import com.archvin.exceptions.RuntimeError
import com.archvin.expression.Expression
import com.archvin.reader.Reader
import com.archvin.token.LiteralToken
import com.archvin.token.OperatorToken
import com.archvin.token.Token
import com.archvin.type.Type
import com.archvin.variable.Variable
import java.util.Stack

class Expressionizer(val resolver: Resolver) : Processor<Expression, Token>() {
    private val manager = this.Manager()

    fun parseDeclaration(r: Reader<Token>, t: Type) {
        val nextToken = r.step()
        if (nextToken !is Token.Identifier) throw CompileError.UnexpectedError("identifier", nextToken.toString())

        val id = nextToken.id

        val variable = Variable(id, t)
        resolver.add(variable)

        manager.add(Expression.Declare(id, t))
    }

    fun parseIdentifier(token: Token.Identifier, r: Reader<Token>) {
        if (token.id == "println") manager.add(Expression.Println) // TODO: replace

        val resolved = resolver.resolve(token.id) ?: throw RuntimeError.UnresolvedIdentifier(token.id)

        when (resolved) {
            is Type -> {
                parseDeclaration(r, resolved)
            }
            is Variable -> manager.add(Expression.Read(resolved))
        }
    }

    override fun step(c: Token) {
        when (c) {
            is Token.Identifier -> parseIdentifier(c, r)
            is LiteralToken<*> -> manager.add(Expression.Literal(c))

            is OperatorToken -> { /*TODO*/ }

            is Token.NullToken -> { /*TODO*/ }
            is Token.Test -> { /*TODO*/ }
        }
    }

    private data class PendingExpr(val expr: Expression, var counter: Int)
    private inner class Manager {
        private val pending = Stack<PendingExpr>()

        fun hasPending() = pending.isNotEmpty()

        fun add(baseExpr: Expression) {
            if (hasPending()) {
                val top = pending.peek()
                val types = top.expr.paramTypes
                baseExpr.asserType(types[types.size - top.counter])
            }

            val counter = baseExpr.paramTypes.size
            pending.push(PendingExpr(baseExpr, counter))

            if (counter == 0) pop()
        }

        fun pop() {
            val expr = pending.pop().expr
            ret.add(expr)

            if (hasPending() && --pending.peek().counter <= 0) pop()
        }

    }
}