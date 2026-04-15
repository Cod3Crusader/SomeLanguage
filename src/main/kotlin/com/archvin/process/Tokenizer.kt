package com.archvin.process

import com.archvin.exceptions.CompileError
import com.archvin.reader.Reader
import com.archvin.token.LiteralToken
import com.archvin.token.OperatorToken
import com.archvin.token.Token

object Tokenizer : Processor<Token, Char> {
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

            '(' -> Token.TestToken("(")
            ')' -> Token.TestToken(")")
            '{' -> Token.TestToken("{")
            '}' -> Token.TestToken("}")
            ',' -> Token.TestToken(",")
            '&' -> Token.TestToken("&")
            '=' -> OperatorToken(OperatorToken.OpType.ASSIGNMENT)
            '+' -> OperatorToken(OperatorToken.OpType.ADDITION)
            '-' -> OperatorToken(OperatorToken.OpType.SUBTRACTION)
            '*' -> OperatorToken(OperatorToken.OpType.MULTIPLICATION)
            '/' -> OperatorToken(OperatorToken.OpType.DIVISION)
            else -> throw CompileError.UnknownCharacterError("$c")
        }
    }

    private fun tokenizeNumber(raw: String): LiteralToken.NumberLiteral<*> {
        val value = raw.toIntOrNull()
        value?.let { return LiteralToken.NumberLiteral.I32Literal(value) }
        error("$raw cannot be converted to i32, other types are TODO") // TODO
    }

    override fun step(c: Char, r: Reader<Char>): Token? {
        when {
            c.isWhitespace() -> {}
            c == '/' && r.peek() == '/' -> while (!r.isEof() && r.current() != '\n') r.step()
            c.isSimple() -> {
                var raw = "$c"
                while (r.peek().isSimple()) raw += r.step()
                return if (raw[0].isDigit()) tokenizeNumber(raw) else Token.IdentifierToken(raw)
            }
            else -> {
                return tokenizeSpecial(c, r)
            }
        }

        return null
    }
}