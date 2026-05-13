package com.archvin.token

import com.debug.DebugString

sealed class Token : DebugString() {
    class Test(val raw: String) : Token() // TODO: remove
    class Identifier(val id: String) : Token()
    object NullToken : Token()
}

