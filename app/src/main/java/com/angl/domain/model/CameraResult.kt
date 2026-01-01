package com.angl.domain.model

/**
 * Represents the result of a camera operation.
 * Sealed class to ensure type-safe handling of success and error cases.
 */
sealed class CameraResult<out T> {
    data class Success<T>(val data: T) : CameraResult<T>()
    data class Error(val exception: Throwable, val message: String) : CameraResult<Nothing>()
}

/**
 * Extension function to execute code block on success.
 */
inline fun <T> CameraResult<T>.onSuccess(action: (T) -> Unit): CameraResult<T> {
    if (this is CameraResult.Success) action(data)
    return this
}

/**
 * Extension function to execute code block on error.
 */
inline fun <T> CameraResult<T>.onError(action: (Throwable, String) -> Unit): CameraResult<T> {
    if (this is CameraResult.Error) action(exception, message)
    return this
}
