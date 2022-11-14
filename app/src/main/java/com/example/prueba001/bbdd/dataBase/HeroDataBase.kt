package com.example.prueba001.bbdd.dataBase

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.prueba001.bbdd.dao.HeroDao
import com.example.prueba001.bbdd.models.HeroDbModel

@Database(
    entities = [HeroDbModel::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = HeroDataBase.RemoveFavoriteMigration::class)
    ]
)

abstract class HeroDataBase : RoomDatabase() {

    abstract fun getHeroDao(): HeroDao

    @DeleteColumn(
        tableName = "hero_list",
        columnName = "isFavorite"
    )

    class RemoveFavoriteMigration : AutoMigrationSpec {}

    companion object {

        @Volatile
        private var INSTANCE: HeroDataBase? = null

        private const val DB_NAME = "hero_database.db"

        fun getDatabase(context: Context): HeroDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HeroDataBase::class.java,
                    DB_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}