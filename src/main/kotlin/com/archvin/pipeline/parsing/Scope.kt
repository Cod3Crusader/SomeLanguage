package com.archvin.pipeline.parsing

class Scope {
    val declarations = mutableListOf<AstNode.Declaration>()
    val expressions = mutableListOf<Expression>()

    fun addDec(dec: AstNode.Declaration) = declarations.add(dec)
    fun addExpr(expr: Expression) = expressions.add(expr)

    fun add(add: AstNode) {
        when (add) {
            is AstNode.Declaration -> declarations.add(add)
            is Expression -> expressions.add(add)
        }
    }
}