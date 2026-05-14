package com.archvin.variable

import com.archvin.debug.Debug
import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.Type

class Variable(override val id: String, override val type: Type, var value: Value = Value.Uninitialized) : Debug(), HasId, HasType {
    override val className: String = "Var"
}