package edu.illinois.cs.cs124.ay2025.mp.helpers

class ResultMightThrow<T> {
    private val savedValue: T?
    val value: T
        get() {
            if (exception != null) {
                throw exception
            }
            return savedValue!!
        }

    val exception: Exception?

    constructor(setValue: T) {
        savedValue = setValue
        exception = null
    }

    constructor(setException: Exception?) {
        savedValue = null
        exception = setException
    }
}
