package com.archvin.token

import com.archvin.exceptions.CompileError

class Tokenizer(val r: Reader) {
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

    private fun tokenizeSpecial(c: Char): Token {
        return when (c) {
            '"' -> {
                var raw = ""
                while (r.step() != '"') {
                    raw +=
                        if (r.current() == '\\') parseEscape(r.step())
                        else r.current()
                    if (r.index == r.content.length - 1) throw CompileError.UnclosedError("string literal")
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
            '+' -> Token.TestToken("+")
            '-' -> Token.TestToken("-")
            '*' -> Token.TestToken("*")
            '/' -> Token.TestToken("/")
            '=' -> Token.TestToken("=")
            else -> throw CompileError.UnknownCharacterError("$c")
        }
    }

    private fun tokenizeNumber(raw: String): LiteralToken.NumberLiteral<*> {
        val value = raw.toIntOrNull()
        value?.let { return I32Literal(value) }
        error("$raw cannot be converted to i32, other types are TODO") // TODO
    }

    private fun tokenizeSymbol(raw: String): Token.TestToken {
        return Token.TestToken(raw)
    }
    
    fun tokenize(): List<Token> {
        val out = ArrayList<Token>()

        r.reset()
        while (!r.isEof()) {
            val c = r.current()
            when {
                c == '/' && r.peek() == '/' -> while(!r.isEof() && r.current() != '\n') r.step()
                c.isSimple() -> {
                    var raw = "$c"
                    while (r.step().isSimple()) raw += r.current()
                    out.add(if (raw[0].isDigit()) tokenizeNumber(raw) else tokenizeSymbol(raw))
                }
                c.isWhitespace() -> r.step()
                else -> {
                    out.add(tokenizeSpecial(c))
                    r.step()
                }
            }
        }

        return out
    }
}