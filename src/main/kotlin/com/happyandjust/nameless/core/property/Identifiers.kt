package com.happyandjust.nameless.core.property

import com.happyandjust.nameless.gui.feature.components.Identifier

@kotlinx.serialization.Serializable
data class Identifiers<T : Identifier>(val selected: List<T>) : List<T> by selected