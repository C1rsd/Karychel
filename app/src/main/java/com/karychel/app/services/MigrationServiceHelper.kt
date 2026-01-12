package com.karychel.app.services

import android.content.Context
import android.content.Intent

/**
 * Helper para iniciar y detener el MigrationService de forma sencilla
 */
object MigrationServiceHelper {

    /**
     * Inicia la migración de un archivo de backup
     * @param context Contexto de la aplicación
     * @param backupPath Ruta completa al archivo .7z de backup
     */
    fun startMigration(context: Context, backupPath: String) {
        val intent = Intent(context, MigrationService::class.java).apply {
            action = MigrationService.ACTION_START_MIGRATION
            putExtra(MigrationService.EXTRA_BACKUP_PATH, backupPath)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Detiene la migración en curso
     * @param context Contexto de la aplicación
     */
    fun stopMigration(context: Context) {
        val intent = Intent(context, MigrationService::class.java).apply {
            action = MigrationService.ACTION_STOP_MIGRATION
        }
        context.startService(intent)
    }
}
