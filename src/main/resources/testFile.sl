i32 add5(i32 a) {
    i32 add3(i32 b) {
        return add(b, 3)
    }

    return add(add3(a), 2)
}

void println(str msg) {
    print(msg)
    print('\n')
}


i32 a = 3
println(add5(a))
