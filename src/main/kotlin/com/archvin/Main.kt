package com.archvin

import com.archvin.pipeline.execution.Runner
import com.archvin.pipeline.finalizing.Instruction
import com.archvin.pipeline.finalizing.NameResolver
import com.archvin.pipeline.finalizing.TypeChecker
import com.archvin.pipeline.lexing.SpecialToken
import com.archvin.pipeline.lexing.Tokenizer
import com.archvin.pipeline.parsing.Expression
import com.archvin.pipeline.parsing.Parser
import com.archvin.reader.SimpleReader
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

    val resolver = NameResolver()

    val tokens = Tokenizer().process(charReader)
    val expr = Parser().process(SimpleReader(tokens, SpecialToken.NewLine))
    expr.forEach { println(it) }
    val instr = TypeChecker().process(SimpleReader(expr, Expression.PassExpr))

    println()
    Runner().process(SimpleReader(instr, Instruction.PassInstr))
}