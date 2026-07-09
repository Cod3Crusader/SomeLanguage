package com.archvin.pipeline.typecheck

import com.archvin.data.symbol.Symbol
import com.archvin.data.type.Type
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.execution.RuntimeScope

class ScopeBuilder {
    private val used = setOf<String>()
    private val symbols = ArrayList<Symbol>()

    data class Built(val resolver: Context, val scope: RuntimeScope)
    fun build(retType: Type, ctxType: Context.ContextType, parentRes: Context?): Built {
        val scope = RuntimeScope(symbols.size)
        return Built(Context(symbols, scope, ctxType, retType, parentRes), scope)
    }


    fun <T : Symbol> addSymbol(add: T) {
        val id = add.id

        if (used.contains(id)) throw CompileError.Redeclaration(id)

        symbols.add(add)
    }
}