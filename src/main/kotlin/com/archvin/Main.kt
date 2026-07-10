package com.archvin

//import com.archvin.pipeline.typecheck.TypeChecker
import com.archvin.pipeline.execution.Runner
import com.archvin.pipeline.lexing.Tokenizer
import com.archvin.pipeline.parsing.Parser
import com.archvin.pipeline.typecheck.TypeChecker
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

    val charReader = SimpleReader(code.toCharArray().toList())

    val tokens = Tokenizer.process(charReader)
    val parsed = Parser.process(SimpleReader(tokens))
    //expr.forEach { println(it) }
    val main = TypeChecker.process(parsed)
    //println()
    //main.instructions.forEach { println(it) }


    println()
    Runner.process(main)
}