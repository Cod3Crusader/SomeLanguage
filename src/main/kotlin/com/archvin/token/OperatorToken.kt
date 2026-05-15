package com.archvin.token

sealed class OperatorToken(raw: String) : Token(raw) {
    object Assignment : OperatorToken("=")
    object Addition : OperatorToken("+")
    object Subtraction : OperatorToken("-")
    object Multiplication : OperatorToken("*")
    object Division : OperatorToken("/")
    object OpenBracket : OperatorToken("(")
    object CloseBracket : OperatorToken(")")
    object Comma : OperatorToken(",")
}
