package com.archvin

import com.archvin.exceptions.RuntimeError
import com.archvin.type.CharType
import com.archvin.type.HasId
import com.archvin.type.I32Type
import com.archvin.type.StrType
import com.archvin.type.Type
import com.archvin.type.VoidType
import com.archvin.variable.Variable

object Resolver {
    private val map = mutableMapOf<String, HasId>()

    init {
        add(I32Type)
        add(CharType)
        add(StrType)
        add(VoidType)
        add(HasId.TODO("println"))
    }

    fun add(value: HasId) {
        val id = value.id
        if (map.containsKey(id)) throw RuntimeError.Redeclaration(id)
        map[id] = value
    }

    fun resolve(id: String): HasId? = map[id]
    fun resolveType(id: String): Type? = map[id] as? Type
    fun resolveVar(id: String): Variable? = map[id] as? Variable
}