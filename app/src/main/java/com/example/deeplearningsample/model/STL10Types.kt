package com.example.deeplearningsample.model

import java.lang.RuntimeException


fun indexOf(id: Int): STL10Class {
    STL10Class.values().forEach { value ->
        run {
            if (id == value.id) {
                return@indexOf value
            }
        }
    }
    throw RuntimeException("Invalid class: ID ${id}")
}

enum class STL10Class(val id: Int, val className: String) {
    AIRPLANE(0, "airplane"),
    BIRD(1, "bird"),
    CAR(2, "car"),
    CAT(3, "cat"),
    DEER(4, "deer"),
    DOG(5, "dog"),
    HORSE(6, "horse"),
    MONKEY(7, "monkey"),
    SHIP(8, "ship"),
    TRUCK(9, "truck")
}