i32 add5(i32 a) {
    i32 add3(i32 b) {
        return add(b, 3)
    }

    if (true) {
        i32 add2(i32 c) {
            return add(c, 2)
        }

        return add2(a)
    }

    return add(add3(a), 2)
}

void println(str msg) {
    print(msg)
    print('\n')
}

void printNum(i32 num) {
    print(num)
    print('\n')
}

i32 a = 3
printNum(add5(a))

if (true) {
    println("This will not be printed")
} else {
    println("This will be printed")
}

println("this will also be printed")