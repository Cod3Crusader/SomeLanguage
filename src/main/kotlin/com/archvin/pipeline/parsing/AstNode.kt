package com.archvin.pipeline.parsing

import com.archvin.pipeline.parsing.Expression.AssignExpr
import com.archvin.utils.Debug

sealed class AstNode : Debug() {
    sealed class Declaration(val id: String) : AstNode() {
        class VarDeclare(id: String, val typeId: String, val init: AssignExpr, val isMutable: Boolean = false) : Declaration(id)
        class FunDeclare(id: String, val retType: String, val paramTypes: List<String>, val scope: Scope)
            : Declaration(id)
    }
}