//!DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@kotlin.jvm.JvmName("containsAny")
@kotlin.internal.LowPriorityInOverloadResolution
public fun <T> Iterable<T>.contains1(element: T): Int = null!!

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
public fun <T> Iterable<T>.contains1(element: @kotlin.internal.NoInfer T): Boolean = null!!


fun test() {
    val a: Int = listOf(1).contains1("")
    val b: Boolean = listOf(1).contains1(1)
}