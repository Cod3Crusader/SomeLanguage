package com.archvin.pipeline.typecheck

import com.archvin.data.scope.Scope
import com.archvin.data.symbol.Symbol
import com.archvin.exceptions.CompileError

class ScopeBuilder(val parent: ScopeBuilder? = null) {
    private val map = mutableMapOf<String, Stored<*>>()
    private var index = 0

    private var finished = false
    private lateinit var scope: Scope

    fun finish() {
        finished = true
        // TODO: statics
        scope = Scope(ArrayList(), index)
    }

    fun <T : Symbol> addSymbol(add: T): Stored<T> {
        if (finished) error("no more variables can be added to a finished scope")

        val id = add.id

        if (map.containsKey(id)) throw CompileError.Redeclaration(id)

        val stored = Stored(add, index++)

        map[id] = stored
        return stored
    }

    fun tryResolve(id: String): Resolved<*>? {
        if (!finished) error("reading variable before scope is built")
        return map[id]?.let { Resolved(it, this.scope) } ?: parent?.tryResolve(id)
    }
    fun resolve(id: String): Resolved<*> = tryResolve(id) ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveFunc(id: String): Resolved<Symbol.Function> {
        val res = resolve(id).asT<Symbol.Function>() ?: throw CompileError.UnresolvedIdentifier(id)

        return res
    }
    fun resolveVar(id: String) = resolve(id).asT<Symbol>() ?: throw CompileError.UnresolvedIdentifier(id)

    data class Stored<T : Symbol>(val obj: T, val index: Int)

    data class Resolved<out T : Symbol>(val res: T, val scope: Scope, val index: Int) {
        inline fun <reified T : Symbol> asT() = (res as? T)?.let { Resolved(it, scope, index) }
        constructor(stored: Stored<T>, scope: Scope) : this(stored.obj, scope, stored.index)
    }
}