package com.htueko.android.imagedetectiondemo.data

data class Recognition(
    val name: String,
    val probability: Float
) {
    override fun toString() =
        "$name : ${probability*100}%"
}