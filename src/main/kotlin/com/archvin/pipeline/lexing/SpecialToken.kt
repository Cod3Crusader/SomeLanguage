package com.archvin.pipeline.lexing

sealed class SpecialToken(raw: String) : Token(raw) {
    object Assignment : SpecialToken("=")
    object Addition : SpecialToken("+")
    object Subtraction : SpecialToken("-")
    object Multiplication : SpecialToken("*")
    object Division : SpecialToken("/")
    object OpenBracket : SpecialToken("(")
    object CloseBracket : SpecialToken(")")
    object Comma : SpecialToken(",")
    object NewLine : SpecialToken("newline")
}
