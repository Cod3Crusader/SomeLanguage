package com.archvin.pipeline.parsing

import com.archvin.pipeline.parsing.AstNode.Declaration
import com.archvin.pipeline.parsing.AstNode.Declaration.VarDeclare

class Scope {
    val varDeclares = mutableListOf<Declaration>()
    val expressions = mutableListOf<Expression>()

    fun addDec(dec: Declaration) = varDeclares.add(dec)
    fun addExpr(expr: Expression) = expressions.add(expr)

    fun add(add: AstNode) {
        when (add) {
            is Declaration -> {
                varDeclares.add(add)
                if (add is VarDeclare) addExpr(add.init)
            }
            is Expression -> expressions.add(add)
        }
    }
}