package com.archvin.variable

import com.archvin.type.HasId
import com.archvin.type.HasType
import com.archvin.type.Type
import com.archvin.utils.Debug

class Variable(override val id: String,
               override val type: Type,
               val isMutable: Boolean) : Debug(), HasId, HasType {
    var value: Value = Value.Uninitialized

    constructor(id: String, value: Value) : this(id, value.type, false) {
        this.value = value
    }
}
