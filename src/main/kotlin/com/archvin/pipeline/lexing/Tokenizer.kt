package com.archvin.pipeline.lexing

import com.archvin.data.variable.Literal
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.Stage
import com.archvin.reader.Reader

class Tokenizer : Stage<Token, Char>() {
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

                Token.LiteralToken(Literal.StringLiteral(raw))
            }

            '\'' -> {
                val char =
                    if (r.step() == '\\') parseEscape(r.step())
                    else r.current()
                if (r.step() != '\'') throw CompileError.UnclosedError("character literal")

                Token.LiteralToken(Literal.CharLiteral(char))
            }

            '(' -> SpecialToken.OpenBracket
            ')' -> SpecialToken.CloseBracket
            '{' -> Token.Test("{")
            '}' -> Token.Test("}")
            ',' -> SpecialToken.Comma
            '&' -> Token.Test("&")
            '=' -> SpecialToken.Assignment
            '+' -> SpecialToken.Addition
            '-' -> SpecialToken.Subtraction
            '*' -> SpecialToken.Multiplication
            '/' -> SpecialToken.Division
            else -> throw CompileError.UnknownCharacterError("$c")
        }
    }

    private fun tokenizeNumber(raw: String): Token.LiteralToken<*> {
        val value = raw.toIntOrNull()
        value?.let { return Token.LiteralToken(Literal.NumberLiteral.I32Literal(value)) }
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
                    else Token.IdentifierToken(raw)
                )
            }
            else -> {
                yield(tokenizeSpecial(c, r))
            }
        }
    }
}