package com.archvin.token

class OperatorToken(val opType: OpType) : Token() {
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
