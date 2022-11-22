package com.example.prueba001.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prueba001.bbdd.models.HeroDbModel
import com.example.prueba001.bbdd.repository.DataBaseRepository
import com.example.prueba001.model.HeroModel
import com.example.prueba001.repository.HeroRepository
import com.example.prueba001.utils.mapToDb
import com.example.prueba001.utils.mapToModel
import com.example.prueba001.utils.setListWithFavorites
import com.example.prueba001.viewModels.state.ListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val heroRepository: HeroRepository,
    private val dataBaseRepository: DataBaseRepository
) : ViewModel() {

    val state: StateFlow<ListState> get() = _state.asStateFlow()

    private val _state = MutableStateFlow(ListState())

    private val originalHeroes = MutableStateFlow(emptyList<HeroModel>())

    private lateinit var favorites: StateFlow<List<HeroDbModel>>

    init {
        viewModelScope.launch {
            favorites = dataBaseRepository.getHeroesFromDataBase().stateIn(scope = CoroutineScope(Job()))
            getHeroes()
            favorites.collect {
                val heroes = _state.value.heroList
                setHeroesWithFavorites(heroes)
            }
        }
    }

    fun setFav(hero: HeroModel) {
        if (!hero.isFavorite) { // funciona al revés porque ya se ha cambiado la variable fav del objeto
            deleteHero(hero.mapToDb())
        } else {
            insertHero(hero.mapToDb())
        }
    }

    fun refresh(searchText: String) {
        if (searchText.isEmpty()) {
            refresh()
        } else {
            refreshSearch(searchText)
        }
    }

    fun search(searchText: String) {
        val searchHeros = originalHeroes.value.filter { hero ->
            hero.name.uppercase().contains(searchText.uppercase())
        }
        _state.update { it.copy(heroList = searchHeros) }
    }

    private fun refreshSearch(search: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(refreshing = true) }
            getHeroes()
            search(search)
            _state.update { it.copy(refreshing = false) }
        }
    }

    private suspend fun getHeroes() {
        try {
            var heroes = heroRepository.getHeroes()
            setHeroesWithFavorites(heroes)
        } catch (_: SocketTimeoutException) { }
    }

    private fun setHeroesWithFavorites(heroList: List<HeroModel>) {
        var heroes = heroList
        if (heroes.isEmpty()) heroes = favorites.value.mapToModel()
        else heroes.setListWithFavorites(favorites.value)
        _state.update { it.copy(heroList = heroes) }
        originalHeroes.update { heroes }
    }

    private fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(refreshing = true) }
            getHeroes()
            _state.update { it.copy(refreshing = false) }
        }
    }

    private fun insertHero(hero: HeroDbModel) {
        viewModelScope.launch(Dispatchers.IO) {
            dataBaseRepository.insertHero(hero)
        }
    }

    private fun deleteHero(hero: HeroDbModel) {
        viewModelScope.launch(Dispatchers.IO) {
            dataBaseRepository.deleteHero(hero)
        }
    }

}