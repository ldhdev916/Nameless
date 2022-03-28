package com.happyandjust.nameless.hypixel.skyblock.experimentation

import com.happyandjust.nameless.dsl.TempEventListener

interface ExperimentationGame : TempEventListener

interface ExperimentationGameCreator {
    fun createImpl(): ExperimentationGame

    val chestDisplayName: String
}

enum class ExperimentationType : ExperimentationGameCreator {
    Chronomatron {
        override fun createImpl() = Chronomatron()

        override val chestDisplayName = "Chronomatron"
    },
    Superpairs {
        override fun createImpl() = Superpairs()

        override val chestDisplayName = "Superpairs"
    },
    UltraSequencer {
        override fun createImpl() = UltraSequencer()

        override val chestDisplayName = "Ultrasequencer"
    }
}