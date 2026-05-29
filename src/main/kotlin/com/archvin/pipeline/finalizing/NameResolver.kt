package com.archvin.pipeline.finalizing

import com.archvin.exceptions.CompileError
import com.archvin.type.BuiltinType
import com.archvin.type.HasId
import com.archvin.type.Type
import com.archvin.variable.FunctionValue
import com.archvin.variable.Variable

class NameResolver {
    private val map = mutableMapOf<String, HasId>()

    init {
        add(BuiltinType.I32Type)
        add(BuiltinType.CharType)
        add(BuiltinType.StrType)
        add(BuiltinType.VoidType)

        add(Variable("println", FunctionValue.BuiltinFunction.Println))
        add(Variable("add", FunctionValue.BuiltinFunction.Add))
    }

    fun add(value: HasId) {
        val id = value.id
        if (map.containsKey(id)) throw CompileError.Redeclaration(id)
        map[id] = value
    }

    fun tryResolve(id: String): HasId? = map[id]
    fun resolve(id: String): HasId = map[id] ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveType(id: String) = map[id] as? Type ?: throw CompileError.UnresolvedIdentifier(id)
    fun resolveFunc(id: String) = (map[id] as? Variable)?.value as? FunctionValue ?: throw CompileError.InvalidCallError(id)
    fun resolveVar(id: String) = map[id] as? Variable ?: throw CompileError.UnresolvedIdentifier(id)

}