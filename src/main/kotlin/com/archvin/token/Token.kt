package com.archvin.token

import com.archvin.debug.Debug

sealed class Token(val raw: String) : Debug() {
    class Test(raw: String) : Token(raw) // TODO: remove
    class Identifier(val id: String) : Token(id)
    object NullToken : Token("null")
}

