package com.archvin.exceptions

import com.archvin.token.Token
import com.archvin.type.Type
import com.archvin.variable.Variable

sealed class CompileError(errorMessage: String) : Exception(errorMessage) {
    class TypeMismatchError(expected: Type, got: Type)
        : CompileError("Expected type: ${expected.signature}, but received: ${got.signature}") {}
    class UnknownCharacterError(char: String) : CompileError("Unknown character '$char'")
    class UnexpectedError(expected: String, got: String) : CompileError("Expected: \"$expected\", got \"$got\"")
    class UnclosedError(unclosed: String) : CompileError("Unclosed: \"$unclosed\"")
    class UninitializedError(uninitialized: Variable) : CompileError("\"${uninitialized.id}\" cannot be uninitialized")
    class CannotReassign(variable: Variable.Constant) : CompileError("Cannot reassign constant: \"${variable.id}\"")
    class InvalidCallError(token: Token) : CompileError("\"${token.raw}\" cannot be called")
}