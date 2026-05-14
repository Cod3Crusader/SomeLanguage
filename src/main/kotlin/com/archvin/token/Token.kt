package com.archvin.token

import com.archvin.debug.Debug

sealed class Token : Debug() {
    class Test(val raw: String) : Token() // TODO: remove
    class Identifier(val id: String) : Token()
    object NullToken : Token()
}

