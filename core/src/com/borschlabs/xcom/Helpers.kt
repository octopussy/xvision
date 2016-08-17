package com.borschlabs.xcom

/**
 * @author octopussy
 */

fun <T : Comparable<T>> T.clamp(min: T, max: T): T = when {
    this < min -> min
    this > max -> max
    else -> this
}