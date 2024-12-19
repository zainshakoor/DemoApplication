package com.dev.demoapplication


// Higher-order function
fun performOperation(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b) // Executes the passed lambda
}

fun main() {
    val a = 10
    val b = 5

    // 1. Using Lambda Inline
    val sumInline = performOperation(a, b) { x, y -> x + y }
    println("Sum (Inline Lambda): $sumInline") // Output: Sum (Inline Lambda): 15

    // 2. Using a Multi-Line Lambda
    val subtractMultiLine = performOperation(a, b) { x, y ->
        println("Subtracting $y from $x")
        x - y
    }
    println("Difference (Multi-Line Lambda): $subtractMultiLine") // Output: Difference (Multi-Line Lambda): 5

    // 3. Using Lambda Stored in a Variable
    val multiply: (Int, Int) -> Int = { x, y -> x * y }
    val product = performOperation(a, b, multiply)
    println("Product (Lambda Variable): $product") // Output: Product (Lambda Variable): 50

    // 4. Using Anonymous Function (Alternative to Lambda)
    val divide = fun(x: Int, y: Int): Int {
        if (y == 0) {
            println("Cannot divide by zero")
            return 0
        }
        return x / y
    }
    val quotient = performOperation(a, b, divide)
    println("Quotient (Anonymous Function): $quotient") // Output: Quotient (Anonymous Function): 2

    // 5. Passing a Function Reference
    val modulus = performOperation(a, b, ::findModulus)
    println("Modulus (Function Reference): $modulus") // Output: Modulus (Function Reference): 0
}

// A regular function for modulus
fun findModulus(x: Int, y: Int): Int = x % y
