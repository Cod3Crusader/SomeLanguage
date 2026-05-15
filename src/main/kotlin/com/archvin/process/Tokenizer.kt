package com.archvin.process

import com.archvin.exceptions.CompileError
import com.archvin.reader.Reader
import com.archvin.token.LiteralToken
import com.archvin.token.OperatorToken.*
import com.archvin.token.Token

class Tokenizer : Processor<Token, Char>() {
    private fun Char.isSimple(): Boolean = isLetterOrDigit() || this == '_'

    private fun parseEscape(c: Char): Char = when (c) {
        '\\' -> '\\'
        'n' -> '\n'
        'r' -> '\r'
        't' -> '\t'
        'b' -> '\b'
        '\'' -> '\''
        else -> throw CompileError.UnknownCharacterError("\\$c")
    }

    private fun tokenizeSpecial(c: Char, r: Reader<Char>): Token {
        return when (c) {
            '"' -> {
                var raw = ""
                while (r.step() != '"') {
                    raw +=
                        if (r.current() == '\\') parseEscape(r.step())
                        else r.current()
                    if (r.index == r.getAll().size - 1) throw CompileError.UnclosedError("string literal")
                }

                LiteralToken.StringLiteral(raw)
            }

            '\'' -> {
                val char =
                    if (r.step() == '\\') parseEscape(r.step())
                    else r.current()
                if (r.step() != '\'') throw CompileError.UnclosedError("character literal")

                LiteralToken.CharLiteral(char)
            }

            '(' -> OpenBracket
            ')' -> CloseBracket
            '{' -> Token.Test("{")
            '}' -> Token.Test("}")
            ',' -> Token.Test(",")
            '&' -> Token.Test("&")
            '=' -> Assignment
            '+' -> Addition
            '-' -> Subtraction
            '*' -> Multiplication
            '/' -> Division
            else -> throw CompileError.UnknownCharacterError("$c")
        }
    }

    private fun tokenizeNumber(raw: String): LiteralToken.NumberLiteral<*> {
        val value = raw.toIntOrNull()
        value?.let { return LiteralToken.NumberLiteral.I32Literal(value) }
        error("$raw cannot be converted to i32") // TODO
    }

    override fun step(c: Char) {
        when {
            c.isWhitespace() -> {}
            c == '/' && r.peek() == '/' -> while (!r.isEof() && r.current() != '\n') r.step()
            c == '/' && r.peek() == '*' -> while(!r.isEof() && !(r.current() == '/' && r.peek(-1) == '*')) r.step()
            c.isSimple() -> {
                var raw = "$c"
                while (r.peek().isSimple()) raw += r.step()
                yield(
                    if (raw[0].isDigit()) tokenizeNumber(raw)
                    else Token.Identifier(raw)
                )
            }
            else -> {
                yield(tokenizeSpecial(c, r))
            }
        }
    }
}