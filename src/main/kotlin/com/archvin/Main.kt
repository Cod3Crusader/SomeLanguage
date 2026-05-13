package com.archvin

import com.archvin.process.Compiler
import com.archvin.process.Resolver
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

    val charReader = SimpleReader(code.toCharArray().toList(), 0.toChar())

    val tokens = Tokenizer().process(charReader)
    val tokenReader = SimpleReader(tokens, Token.NullToken)

    val resolver = Resolver()

    val expr = Compiler(resolver).process(tokenReader)
    expr.forEach { println(it) }
}
