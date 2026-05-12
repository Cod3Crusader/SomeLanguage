package com.archvin.token

data class OperatorToken(val opType: OpType) : Token {
    enum class OpType {
        ASSIGNMENT,
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION,
        OPEN_BRACKET,
        CLOSE_BRACKET,
    }
}
