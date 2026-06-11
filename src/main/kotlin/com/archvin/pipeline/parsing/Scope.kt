package com.archvin.pipeline.parsing

class Scope {
    val declarations = mutableListOf<AstNode.Declaration>()
    val expressions = mutableListOf<Expression>()

    fun add(dec: AstNode.Declaration) = declarations.add(dec)
    fun add(expr: Expression) = expressions.add(expr)
}