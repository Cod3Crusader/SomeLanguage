package com.archvin.pipeline.lexing

import com.archvin.data.Literal
import com.archvin.exceptions.CompileError
import com.archvin.pipeline.lexing.SpecialToken.*
import com.archvin.reader.Reader
import com.archvin.utils.readAll

object Tokenizer {
    private lateinit var r: Reader<Char>

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
                        if (r.current() == '\\') parseEscape(r.step()!!)
                        else r.current()
                    if (r.index == r.getAll().size - 1) throw CompileError.UnfinishedError("string literal")
                }

                Token.LiteralToken(Literal.StringLiteral(raw))
            }

            '\'' -> {
                val char =
                    if (r.step() == '\\') parseEscape(r.step()!!)
                    else r.current()
                if (r.step() != '\'') throw CompileError.UnfinishedError("character literal")

                Token.LiteralToken(Literal.CharLiteral(char))
            }

            '(' -> OpenPar
            ')' -> ClosePar
            '{' -> OpenBraces
            '}' -> CloseBraces
            ',' -> Comma
            '&' -> Token.Test("&")
            '=' -> Assignment
            '+' -> Addition
            '-' -> Subtraction
            '*' -> Multiplication
            '/' -> Division
            else -> throw CompileError.UnknownCharacterError("$c")
        }
    }

    private fun tokenizeNumber(raw: String): Token.LiteralToken<*> {
        val value = raw.toIntOrNull()
        value?.let { return Token.LiteralToken(Literal.NumberLiteral.I32Literal(value)) }
        error("$raw cannot be converted to i32") // TODO
    }

    fun consume(c: Char): Token? {
        when {
            c.isWhitespace() -> {}
            c == '/' && r.peek() == '/' -> while (!r.isEof() && r.current() != '\n') r.step()
            c == '/' && r.peek() == '*' -> while(!r.isEof() && !(r.current() == '/' && r.peek(-1) == '*')) r.step()
            c.isSimple() -> {
                var raw = "$c"
                while (r.peek()!!.isSimple()) raw += r.step()

                return if (raw[0].isDigit()) tokenizeNumber(raw) else Token.IdentifierToken(raw)
            }

            else -> return tokenizeSpecial(c, r)
        }

        return next()
    }

    fun next() = r.step()?.let { consume(it) }

    fun process(r: Reader<Char>): List<Token> {
        this.r = r

        val ret = ArrayList<Token>()
        r.readAll { consume(it)?.let { ret.add(it) } }
        return ret
    }
}