package tech.ryadom.origami.util.extensions

import androidx.compose.runtime.MutableState

inline fun <T> MutableState<T>.update(f: (T) -> T) {
    while (true) {
        val prevValue = value
        val nextValue = f(prevValue)

        if (value == prevValue) {
            value = nextValue
            return
        }
    }
}