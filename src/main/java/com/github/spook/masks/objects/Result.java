package com.github.spook.masks.objects;

@SuppressWarnings("unused")
public class Result<T, E> {

    private final T value; // The value of the result.
    private final E error; // The error of the result.
    private final boolean success; // Whether the result was successful.


    public Result(T value, E error) {
        this.value = value; // Set the value.
        this.error = error; // Set the error.
        this.success = error == null; // Set the success.
    }

    /** Returns the value of the result. */
    public T getValue() {
        return value;
    }

    /** Returns the error of the result. */
    public E getError() {
        return error;
    }

    /** Returns whether the result was successful. */
    public boolean isSuccess() {
        return success;
    }

    /** Returns whether the result was unsuccessful. */
    public boolean isError() {
        return !success;
    }

    // Self-explanatory.

    public static <T, E> Result<T, E> success(T value) {
        return new Result<>(value, null);
    }

    public static <T, E> Result<T, E> error(E error) {
        return new Result<>(null, error);
    }

    public static <T, E> Result<T, E> error(T value, E error) {
        return new Result<>(value, error);
    }

}
