package com.example.prueba001.composable.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prueba001.bbdd.viewmodel.DataBaseViewModel
import com.example.prueba001.composable.detail.DetailComposable
import com.example.prueba001.composable.list.ListComposable
import com.example.prueba001.model.HeroModel
import com.example.prueba001.utils.decode
import com.example.prueba001.utils.encode
import com.example.prueba001.utils.fromJson
import com.example.prueba001.utils.toJson
import com.example.prueba001.viewModels.HeroViewModel

@Composable
fun NavigationController(
    navController: NavHostController = rememberNavController(),
    heroViewModel: HeroViewModel = hiltViewModel(),
    dbViewModel: DataBaseViewModel = hiltViewModel()
) {
    NavHost(navController = navController, startDestination = Destinations.List.route) {
        composable(Destinations.List.route) { ListComposable(heroViewModel, dbViewModel) { hero ->
            hero.toJson().encode()?.let { encoded ->
                navController.navigate(Destinations.Detail.createRoute(encoded))
            }
        } }
        composable(Destinations.Detail.route) { navBackEntry ->
            val encoded = navBackEntry.arguments?.getString("hero") ?: return@composable

            encoded.decode()?.let { decoded ->
                DetailComposable(hero = decoded.fromJson(HeroModel::class.java), dbViewModel = dbViewModel)
            }
        }
    }
}