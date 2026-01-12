package com.karychel.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Base de datos principal de la aplicación.
 * Configurada para ser eficiente con más de 5,000 registros sin lag.
 */
@Database(
    entities = [MangaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mangaDao(): MangaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DATABASE_NAME = "karychel_database"

        /**
         * Obtiene la instancia de la base de datos (Singleton).
         * Configurada con optimizaciones para rendimiento.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .apply {
                        // Optimizaciones para rendimiento con grandes volúmenes de datos
                        // Habilitar consultas en el hilo principal (útil para queries simples)
                        // .allowMainThreadQueries() // Descomentar solo si es necesario
                        
                        // Configurar callbacks para optimizaciones adicionales
                        addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                                // Optimizaciones de SQLite para mejor rendimiento
                                db.execSQL("PRAGMA journal_mode = WAL;") // Write-Ahead Logging
                                db.execSQL("PRAGMA synchronous = NORMAL;") // Balance entre seguridad y velocidad
                                db.execSQL("PRAGMA cache_size = 10000;") // Cache de 10MB
                                db.execSQL("PRAGMA temp_store = MEMORY;") // Almacenar temporales en memoria
                            }

                            override fun onOpen(db: SupportSQLiteDatabase) {
                                super.onOpen(db)
                                // Aplicar optimizaciones cada vez que se abre la BD
                                db.execSQL("PRAGMA journal_mode = WAL;")
                                db.execSQL("PRAGMA synchronous = NORMAL;")
                                db.execSQL("PRAGMA cache_size = 10000;")
                                db.execSQL("PRAGMA temp_store = MEMORY;")
                                db.execSQL("PRAGMA optimize;") // Optimizar índices
                            }
                        })
                    }
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Cierra la base de datos (útil para testing).
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
