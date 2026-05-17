package com.archvin.parser

import com.archvin.exceptions.RuntimeError
import com.archvin.type.BuiltinType
import com.archvin.type.HasId
import com.archvin.type.Type
import com.archvin.variable.FunctionValue
import com.archvin.variable.Variable

internal class Resolver {
    private val map = mutableMapOf<String, HasId>()

    init {
        add(BuiltinType.I32Type)
        add(BuiltinType.CharType)
        add(BuiltinType.StrType)
        add(BuiltinType.VoidType)

        add(Variable.Constant("println", FunctionValue.BuiltinFunction.Println))
        add(Variable.Constant("add", FunctionValue.BuiltinFunction.Add))
    }

    fun add(value: HasId) {
        val id = value.id
        if (map.containsKey(id)) throw RuntimeError.Redeclaration(id)
        map[id] = value
    }

    fun resolve(id: String): HasId? = map[id]
    fun resolveType(id: String): Type? = map[id] as? Type
    fun resolveVar(id: String): Variable.Mutable? = map[id] as? Variable.Mutable
}