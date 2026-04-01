package com.archvin.token

import java.io.File

class Reader {
    val nullChar: Char = 0.toChar()

    val content: String
    var index = 0
        private set(newIndex) { field = newIndex.coerceIn(0, content.length) }

    constructor(file: File) {
        if (!file.exists() || !file.isFile) {
            error("ERROR: file \"${file.name}\" does not exist or is not file")
        }
        if (!file.canRead()) {
            error("ERROR: can not read file")
        }

        content = file.readText()
    }

    constructor(fname: String): this(File(fname))

    fun current(): Char = if (index < content.length) content[index] else nullChar

    fun reset() { index = 0 }

    fun step(): Char {
        index++
        return current()
    }

    fun get(i: Int): Char = if (i < content.length && i > 0) content[i] else nullChar
    fun peek(i: Int = 1): Char = get(index + i)
    fun isEof(): Boolean = index >= content.length

}