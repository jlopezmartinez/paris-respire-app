package airparis.base

actual fun runBlocking(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking { block() }
}
