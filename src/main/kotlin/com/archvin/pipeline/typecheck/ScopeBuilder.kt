package com.archvin.pipeline.typecheck

import com.archvin.data.symbol.Symbol
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.execution.RuntimeScope

class ScopeBuilder {
    private val used = setOf<String>()
    private val symbols = ArrayList<Symbol>()


    data class Built(val resolver: NameResolver, val scope: RuntimeScope)
    fun build(parentRes: NameResolver?): Built {
        val scope = RuntimeScope(symbols.size)
        return Built(NameResolver(symbols, scope, parentRes), scope)
    }


    fun <T : Symbol> addSymbol(add: T) {
        val id = add.id

        if (used.contains(id)) throw CompileError.Redeclaration(id)

        symbols.add(add)
    }
}