package com.archvin.pipeline.finalizing

import com.archvin.data.HasId
import com.archvin.data.symbol.BuiltinFunction
import com.archvin.data.symbol.Symbol
import com.archvin.data.type.BuiltinType
import com.archvin.data.type.Type
import com.archvin.exceptions.CompileError

open class NameResolver(val parent: NameResolver? = null) {
    private val map = mutableMapOf<String, Stored<*>>()
    
    fun <T : HasId> add(value: T): Stored<T> {
        val id = value.id
        if (map.containsKey(id)) throw CompileError.Redeclaration(id)
        val stored =Stored(value, map.size)
        map[id] = stored
        return stored
    }

    fun tryResolve(id: String): Resolved<*>? = map[id]?.let { Resolved(it) } ?: +parent?.tryResolve(id)
    fun resolve(id: String): Resolved<*> = tryResolve(id) ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveType(id: String) = resolve(id).asT<Type.ObjectType>() ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveFunc(id: String): Resolved<Symbol.Function> {
        val res = resolve(id).asT<Symbol>() ?: throw CompileError.UnresolvedIdentifier(id)
        if (!res.res.isFunction()) throw CompileError.UnresolvedIdentifier(id)

        return Resolved(res.res.asFunction()!!, res.level, res.index)
    }
    fun resolveVar(id: String) = resolve(id).asT<Symbol>() ?: throw CompileError.UnresolvedIdentifier(id)
    
    class TopResolver : NameResolver(null) {
        init {
            add(BuiltinType.I32Type)
            add(BuiltinType.CharType)
            add(BuiltinType.StrType)
            add(BuiltinType.VoidType)

            add(BuiltinFunction.Println)
            add(BuiltinFunction.Add)
        }
    }

    data class Stored<T : HasId>(val obj: T, val index: Int)
    data class Resolved<out T : HasId>(val res: T, val level: Int, val index: Int) {
        inline fun <reified T : HasId> asT() = (res as? T)?.let { Resolved(it, level, index) }
        constructor(stored: Stored<T>) : this(stored.obj, 0, stored.index)
    }
    operator fun <T : HasId> Resolved<T>?.unaryPlus() = if (this != null) Resolved<T>(
        this.res,
        this.level +1,
        this.index
    ) else null
}