package com.equijada95.heroapp.presentation.state

import com.equijada95.heroapp.data.api.model.HeroModel

data class ListState (
    val heroList: List<com.equijada95.heroapp.data.api.model.HeroModel> = emptyList(),
    val refreshing: Boolean = false
)

