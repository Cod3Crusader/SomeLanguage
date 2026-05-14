package com.archvin.exceptions

import com.archvin.type.Type

sealed class CompileError(errorMessage: String) : Exception(errorMessage) {
    class TypeMismatchError(expected: Type, got: Type)
        : CompileError("Expected type: ${expected.signature}, but received: ${got.signature}") {}
    class UnknownCharacterError(char: String) : CompileError("Unknown character '$char'")
    class UnexpectedError(expected: String, got: String) : CompileError("Expected: \"$expected\", got \"$got\"")
    class UnclosedError(unclosed: String) : CompileError("Unclosed: \"$unclosed\"")
}