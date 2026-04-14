package com.archvin.variable

import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.NullType
import com.archvin.process.Expressionizer

object NullObject : HasId, HasType {
    override val id = "null"
    override val type = NullType

    init {
        Expressionizer.expressionize()
    }
}