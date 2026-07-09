package com.archvin.exceptions

import com.archvin.data.symbol.Symbol
import com.archvin.data.type.Type
import com.archvin.pipeline.typecheck.Instruction

sealed class CompileError(errorMessage: String) : Exception(errorMessage) {
    class TypeMismatchError(expected: Type, got: Type)
        : CompileError("Expected type: ${expected.signature}, but received: ${got.signature}") {}
    class UnknownCharacterError(char: String) : CompileError("Unknown character '$char'")
    class ExpectationError(expected: String, got: String) : CompileError("Expected: \"$expected\", got \"$got\"")
    class UnexpectedError(message: String) : CompileError("Unexpected $message")
    class UnfinishedError(unfinished: String) : CompileError("A(n) \"$unfinished\" was left unfinished")
    class UninitializedError(id: String) : CompileError("\"$id\" cannot be uninitialized")
    class CannotReassign(variable: Symbol) : CompileError("Cannot reassign constant: \"${variable.id}\"")
    class InvalidCallError(tried: String) : CompileError("\"$tried\" cannot be called")
    class UnresolvedIdentifier(id: String) : CompileError("Unresolved identifier $id")
    class Redeclaration(id: String) : CompileError("Redeclaration for $id")
    class UnfinishedInstruction(instr: Instruction) : CompileError("Unfinished expression: $instr")
    class InvalidArgumentCount(funcName: String, expected: Int, got: Int) : CompileError("Expected $expected arguments for $funcName but received $got")
}