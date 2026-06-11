package com.archvin.pipeline.finalizing

import com.archvin.data.HasId
import com.archvin.data.type.BuiltinType
import com.archvin.data.type.Type
import com.archvin.data.variable.BuiltinFunction
import com.archvin.data.variable.Symbol
import com.archvin.data.variable.Symbol.Variable
import com.archvin.exceptions.CompileError

class NameResolver {
    private val map = mutableMapOf<String, HasId>()

    init {
        add(BuiltinType.I32Type)
        add(BuiltinType.CharType)
        add(BuiltinType.StrType)
        add(BuiltinType.VoidType)

        add(BuiltinFunction.Println)
        add(BuiltinFunction.Add)
    }

    fun add(value: HasId) {
        val id = value.id
        if (map.containsKey(id)) throw CompileError.Redeclaration(id)
        map[id] = value
    }

    fun tryResolve(id: String): HasId? = map[id]
    fun resolve(id: String): HasId = map[id] ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveType(id: String) = map[id] as? Type ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveFunc(id: String) = map[id] as? Symbol.Function ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveVar(id: String) = map[id] as? Variable ?: throw CompileError.UnresolvedIdentifier(id)
}