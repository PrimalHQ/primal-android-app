package net.primal.shared.data.local.encryption

class Encryptable<T> private constructor(val decrypted: T) {
    companion object {
        fun of(value: Int) = Encryptable(decrypted = value)
        fun of(value: String) = Encryptable(decrypted = value)
        fun of(value: List<String>) = Encryptable(decrypted = value)
        fun of(value: Double) = Encryptable(decrypted = value)
        fun of(value: Long) = Encryptable(decrypted = value)
        fun of(value: Boolean) = Encryptable(decrypted = value)
    }
}

fun Int.asEncryptable() = Encryptable.of(this)
fun String.asEncryptable() = Encryptable.of(this)
fun List<String>.asEncryptable() = Encryptable.of(this)
fun Double.asEncryptable() = Encryptable.of(this)
fun Long.asEncryptable() = Encryptable.of(this)
fun Boolean.asEncryptable() = Encryptable.of(this)

inline fun <E, R> Encryptable<out Iterable<E>>.map(transform: (E) -> R) = this.decrypted.map(transform = transform)
inline fun <E, R> Encryptable<out Iterable<E>>.mapNotNull(transform: (E) -> R) =
    this.decrypted.mapNotNull(transform = transform)

inline fun <E> Encryptable<out Iterable<E>>.forEach(action: (E) -> Unit) = this.decrypted.forEach(action = action)

inline fun <E> Encryptable<out Iterable<E>>.onEach(action: (E) -> Unit) = this.decrypted.onEach(action = action)

inline fun <E> Encryptable<out Iterable<E>>.filter(predicate: (E) -> Boolean): List<E> =
    this.decrypted.filter(predicate = predicate)
