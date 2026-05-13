package com.archvin.variable

import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.Type
import com.debug.DebugString

class Variable(override val id: String, override val type: Type) : DebugString(), HasId, HasType {
    override val className: String = "Var"
}