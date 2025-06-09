package dev.kevin.core.network.util

enum class NetworkError : Error {
    REQUEST_FAILED,
    UNAUTHORIZED,
    CONFLICT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    PAYLOAD_TOO_LARGE,
    SERVER_ERROR,
    SERIALIZATION_ERROR,
    UNKNOWN;
}