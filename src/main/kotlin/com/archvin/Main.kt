package com.archvin

import com.archvin.instruction.Instruction
import com.archvin.process.Parser
import com.archvin.process.Runner
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
    val expr = Parser().process(SimpleReader(tokens, Token.PassToken))
    //expr.forEach { println(it) }

    Runner().process(SimpleReader(expr, Instruction.Pass))
}