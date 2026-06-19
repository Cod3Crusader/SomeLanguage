package com.archvin.pipeline.parsing

class LambdaExpr : Expression() {
    val declares = mutableListOf<Declaration>()
    val expressions = mutableListOf<Expression>()

    fun addDec(dec: Declaration) = declares.add(dec)
    fun addExpr(expr: Expression) = expressions.add(expr)

    fun add(add: AstNode) {
        when (add) {
            is Declaration -> {
                addDec(add)
                if (add is Declaration.VarDeclare) addExpr(add.init)
            }
            is Expression -> addExpr(add)
        }
    }
}