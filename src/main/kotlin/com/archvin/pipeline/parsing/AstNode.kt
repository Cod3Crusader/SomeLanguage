package com.archvin.pipeline.parsing

import com.archvin.utils.Debug
import com.archvin.utils.funSignature

sealed class AstNode : Debug() {
    open class Declaration(val id: String, val typeId: String, val isMutable: Boolean) : AstNode() {
        class FunDeclare(id: String, val retType: String, val paramTypes: List<String>, scope: Scope, expressions: List<Expression>)
            : Declaration(id, funSignature(retType, paramTypes), false)
    }
}