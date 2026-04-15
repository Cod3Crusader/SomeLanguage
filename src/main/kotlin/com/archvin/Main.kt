package com.archvin

import com.archvin.process.Expressionizer
import com.archvin.process.Tokenizer
import com.archvin.reader.SimpleReader
import com.archvin.token.Token
import java.io.File

fun main(args: Array<String>) {
    val file = File(args[0])

    if (!file.exists() || !file.isFile) {
        error("ERROR: file \"${file.absoluteFile}\" does not exist or is not file")
    }
    if (!file.canRead()) {
        error("ERROR: can not read file")
    }

    val code = file.readText()

    val charReader = SimpleReader(code.toCharArray().toTypedArray(), 0.toChar())
    val tokens = Tokenizer.process(charReader).toTypedArray()
    val tokenReader = SimpleReader(tokens, Token.NullToken)
    tokens.forEach { println(it) }
    //val expressionizer = Expressionizer(tokenReader)
    //expressionizer.process()
}
