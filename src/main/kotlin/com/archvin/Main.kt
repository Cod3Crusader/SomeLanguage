package com.archvin

import com.archvin.expression.Expression
import com.archvin.instruction.Instruction
import com.archvin.process.NewParser
import com.archvin.process.Runner
import com.archvin.process.Tokenizer
import com.archvin.process.TypeChecker
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

    val tokens = Tokenizer().process(charReader)
    val expr = NewParser().process(SimpleReader(tokens, SpecialToken.NewLine))
    val instr = TypeChecker().process(SimpleReader(expr, Expression.PassExpr))
    //instr.forEach { println(it) }

    //println()
    Runner().process(SimpleReader(instr, Instruction.PassInstr))
}