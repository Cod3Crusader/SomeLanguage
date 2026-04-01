package com.archvin.token

import com.archvin.builtins.I32

data class I32Literal(override val value: Int) : LiteralToken.NumberLiteral<Int>(value) {
    override val type = I32
}