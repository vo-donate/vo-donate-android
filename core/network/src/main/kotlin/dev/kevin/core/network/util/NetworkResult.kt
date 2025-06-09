package dev.kevin.core.network.util

sealed interface NetworkResult<out D, out E : Error> {
    data class Success<out D>(val data: D) : NetworkResult<D, Nothing>
    data class Error<out E : dev.kevin.core.network.util.Error>(val error: E, val message: String? = null) :
        NetworkResult<Nothing, E>
}

typealias EmptyNetworkResult<E> = NetworkResult<Unit, E>

inline fun <T, E : Error, R> NetworkResult<T, E>.map(map: (T) -> R): NetworkResult<R, E> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(map(data))
        is NetworkResult.Error -> NetworkResult.Error(error)
    }
}

fun <T, E : Error> NetworkResult<T, E>.asEmptyDataResult(): EmptyNetworkResult<E> {
    return map {}
}

inline fun <T, E : Error> NetworkResult<T, E>.onSuccess(action: (T) -> Unit): NetworkResult<T, E> {
    return when (this) {
        is NetworkResult.Success -> {
            action(data)
            this
        }

        is NetworkResult.Error -> this
    }
}

inline fun <T, E : Error> NetworkResult<T, E>.onError(action: (E) -> Unit): NetworkResult<T, E> {
    return when (this) {
        is NetworkResult.Success -> this
        is NetworkResult.Error -> {
            action(error)
            this
        }
    }
}