// TARGET_BACKEND: JS_IR
// DONT_TARGET_EXACT_BACKEND: WASM
// PROPERTY_LAZY_INITIALIZATION

// FILE: A.kt
val a1 = "a".also {
    2 + 2
}

object A {
    val ok = "OK"
}

class B {
    val ok = "OK"
}

enum class C {
    OK
}

const val b = "b"

// FILE: main.kt
fun box(): String {
    val foo = A.ok
    val bar = B().ok
    C.OK
    val baz = b
    return if (js("typeof a1") == "undefined") "OK" else "fail"
}