// TARGET_BACKEND: JS_IR
// DONT_TARGET_EXACT_BACKEND: WASM
// PROPERTY_LAZY_INITIALIZATION

// FILE: A.kt
val a = "a".also {
    2 + 2
}

fun foo() =
    2 + 2

// FILE: main.kt
fun box(): String {
    val foo = foo()
    return if (js("typeof a") == "string" && js("a") == "a") "OK" else "fail"
}