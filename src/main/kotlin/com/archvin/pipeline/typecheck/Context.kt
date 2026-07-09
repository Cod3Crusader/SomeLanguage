package com.archvin.pipeline.typecheck

import com.archvin.data.symbol.Symbol
import com.archvin.data.type.Type
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.execution.RuntimeScope

open class Context(symbols: List<Symbol>,
                   val scope: RuntimeScope,
                   val type: ContextType,
                   val retType: Type,
                   val parent: Context? = null) {

    val map: Map<String, Stored<*>>
    init  {
        val map = mutableMapOf<String, Stored<*>>()
        symbols.forEachIndexed { index, symbol -> map[symbol.id] = Stored(symbol, index) }
        this.map = map.toMap()
    }

    fun tryResolve(id: String): Resolved<*>? {
        return map[id]?.let { Resolved(it, this.scope) } ?: parent?.tryResolve(id)
    }
    fun resolve(id: String): Resolved<*> = tryResolve(id) ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveFunc(id: String): Resolved<Symbol.Function> {
        val res = resolve(id).asT<Symbol.Function>() ?: throw CompileError.UnresolvedIdentifier(id)

        return res
    }
    fun resolveVar(id: String) = resolve(id).asT<Symbol>() ?: throw CompileError.UnresolvedIdentifier(id)

    fun getClosestFunction(): Context? = if (type == ContextType.FUNCTION) this else parent?.getClosestFunction()

    data class Resolved<out T : Symbol>(val res: T, val scope: RuntimeScope, val index: Int) {
        inline fun <reified T : Symbol> asT () = (res as? T)?.let { Resolved(it, scope, index) }
        constructor(stored: Stored<T>, scope: RuntimeScope) : this(stored.obj, scope, stored.index)
    }

    data class Stored<T : Symbol>(val obj: T, val index: Int)

    enum class ContextType {
        FUNCTION,
        LAMBDA
    }
}