package com.archvin.variable

import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.Type

data class Variable(override val id: String, override val type: Type) : HasId, HasType {
    override fun toString(): String = "$type $id"
}