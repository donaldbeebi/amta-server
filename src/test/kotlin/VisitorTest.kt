import org.junit.jupiter.api.Test

sealed class Result<R, E> {
    abstract fun accept(visitor: Visitor<R, E>)
    interface Visitor<R, E> {
        fun visit(value: Val<R>)
        fun visit(error: Err<E>)
    }
}

class Val<R>(val value: R) : Result<R, Nothing>() {
    override fun accept(visitor: Visitor<R, Nothing>) = visitor.visit(this)
}

class Err<E>(val error: E) : Result<Nothing, E>() {
    override fun accept(visitor: Visitor<Nothing, E>) = visitor.visit(this)
}


class VisitorTest {
    //val result1: Result<Int, String> = Val(10)
    //val result2: Result<Int, String> = Err("This is an error")
    //val results = listOf(result1, result2)
    @Test
    fun test() {
        println(("test".toRegex()).find("test_1650469235073_redo_1"))
    }
}