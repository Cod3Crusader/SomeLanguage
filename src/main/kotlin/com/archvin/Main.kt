package com.archvin

import com.archvin.parser.Parser
import com.archvin.parser.Resolver
import com.archvin.process.Tokenizer
import com.archvin.reader.SimpleReader
import com.archvin.token.SpecialToken
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

    val resolver = Resolver()

    val tokens = Tokenizer().process(charReader)
    val expr = Parser(resolver).process(SimpleReader(tokens, SpecialToken.NewLine))
    expr.forEach { println(it) }
    //val instr = TypeChecker().process(SimpleReader(expr, Expression.PassExpr))

    //println()
    //Runner().process(SimpleReader(instr, Instruction.PassInstr))
}