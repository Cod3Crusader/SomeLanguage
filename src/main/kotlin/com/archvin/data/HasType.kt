package com.archvin.data

import com.archvin.data.type.Type
import com.archvin.exceptions.CompileError

interface HasType {
    val type: Type

    fun assertType(t2: Type) {
        if (type != t2) throw CompileError.TypeMismatchError(t2, type)
    }
    fun assertType(t2: HasType) {
        assertType(t2.type)
    }
}