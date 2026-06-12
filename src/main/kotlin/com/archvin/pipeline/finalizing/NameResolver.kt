package com.archvin.pipeline.finalizing

import com.archvin.data.HasId
import com.archvin.data.type.BuiltinType
import com.archvin.data.type.Type
import com.archvin.data.variable.BuiltinFunction
import com.archvin.data.variable.Symbol
import com.archvin.exceptions.CompileError

open class NameResolver(val parent: NameResolver? = null) {
    private val map = mutableMapOf<String, HasId>()
    
    fun add(value: HasId) {
        val id = value.id
        if (map.containsKey(id)) throw CompileError.Redeclaration(id)
        map[id] = value
    }

    fun tryResolve(id: String): HasId? = map[id] ?: parent?.tryResolve(id)
    fun resolve(id: String): HasId = tryResolve(id) ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveType(id: String) = resolve(id) as? Type ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveFunc(id: String) = (resolve(id) as? Symbol)?.asFunction() ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveVar(id: String) = resolve(id) as? Symbol ?: throw CompileError.UnresolvedIdentifier(id)
    
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
}