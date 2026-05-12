package com.archvin.type

import com.archvin.exceptions.CompileError

interface HasType {
    val type: Type
    fun asserType(t2: Type) {
        if (!type.matches(t2)) throw CompileError.TypeMismatchError(type, t2)
    }
    fun asserType(t2: HasType) {
        asserType(t2.type)
    }
}