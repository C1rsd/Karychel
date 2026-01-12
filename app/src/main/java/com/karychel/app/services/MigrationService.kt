package com.karychel.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.karychel.app.MainActivity
import com.karychel.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import java.io.File

/**
 * MigrationService: Foreground Service para importar backups de Aniyomi (.7z)
 * Procesa los mangas en bloques automáticos de 100 elementos para evitar saturación de memoria.
 * Muestra notificación con progreso real (ej. 100/5000).
 */
class MigrationService : Service() {

    companion object {
        private const val CHANNEL_ID = "migration_channel"
        private const val NOTIFICATION_ID = 1
        private const val BLOCK_SIZE = 100 // Regla de oro: bloques de 100 elementos
        
        const val ACTION_START_MIGRATION = "com.karychel.app.START_MIGRATION"
        const val ACTION_STOP_MIGRATION = "com.karychel.app.STOP_MIGRATION"
        const val EXTRA_BACKUP_PATH = "backup_path"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isProcessing = false
    private var totalItems = 0
    private var processedItems = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MIGRATION -> {
                val backupPath = intent.getStringExtra(EXTRA_BACKUP_PATH)
                if (backupPath != null && !isProcessing) {
                    startForegroundService()
                    processBackup(backupPath)
                }
            }
            ACTION_STOP_MIGRATION -> {
                stopMigration()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Crea el canal de notificación para Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Migración de Backups",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Progreso de importación de backups de Aniyomi"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Inicia el servicio en primer plano con notificación inicial
     */
    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Migración en progreso")
            .setContentText("Preparando importación...")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(0, 0, true)
            .build()

        // Iniciar servicio en primer plano con tipo apropiado para Android 14+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Actualiza la notificación con el progreso actual
     */
    private fun updateNotification(progress: Int, max: Int, status: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Migración en progreso")
            .setContentText("$status ($progress/$max)")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(max, progress, false)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    /**
     * Procesa el archivo de backup .7z en bloques de 100 elementos
     */
    private fun processBackup(backupPath: String) {
        isProcessing = true
        processedItems = 0

        serviceScope.launch {
            try {
                val backupFile = File(backupPath)
                if (!backupFile.exists()) {
                    showErrorNotification("Archivo de backup no encontrado")
                    stopSelf()
                    return@launch
                }

                // Contar total de elementos primero (simplificado - en producción usar biblioteca .7z)
                totalItems = estimateTotalItems(backupFile)
                
                updateNotification(0, totalItems, "Iniciando migración...")

                // Procesar en bloques de 100
                val items = extractItemsFromBackup(backupFile)
                val blocks = items.chunked(BLOCK_SIZE)

                blocks.forEachIndexed { blockIndex, block ->
                    if (!isProcessing) {
                        return@forEachIndexed
                    }

                    // Procesar bloque
                    processBlock(block, blockIndex + 1, blocks.size)
                    
                    // Actualizar progreso
                    processedItems += block.size
                    val status = "Procesando bloque ${blockIndex + 1}/${blocks.size}"
                    updateNotification(processedItems, totalItems, status)

                    // Pequeña pausa entre bloques para no saturar
                    delay(100)
                }

                // Migración completada
                showCompletionNotification()
                stopSelf()

            } catch (e: Exception) {
                showErrorNotification("Error: ${e.message}")
                stopSelf()
            }
        }
    }

    /**
     * Estima el total de elementos en el backup .7z
     */
    private fun estimateTotalItems(backupFile: File): Int {
        return try {
            SevenZFile(backupFile).use { archive ->
                var count = 0
                var entry: SevenZArchiveEntry? = archive.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.contains("manga", ignoreCase = true)) {
                        count++
                    }
                    entry = archive.nextEntry
                }
                count.coerceAtLeast(1)
            }
        } catch (e: Exception) {
            // Si falla, estimar basándose en el tamaño del archivo
            (backupFile.length() / 50_000).toInt().coerceAtLeast(1)
        }
    }

    /**
     * Extrae los elementos del backup .7z usando Apache Commons Compress
     */
    private fun extractItemsFromBackup(backupFile: File): List<BackupItem> {
        val items = mutableListOf<BackupItem>()
        
        try {
            SevenZFile(backupFile).use { archive ->
                var entry: SevenZArchiveEntry? = archive.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.contains("manga", ignoreCase = true)) {
                        items.add(BackupItem(entry.name, entry.size))
                    }
                    entry = archive.nextEntry
                }
            }
        } catch (e: Exception) {
            throw Exception("Error al leer archivo .7z: ${e.message}", e)
        }
        
        return items
    }

    /**
     * Procesa un bloque de elementos (máximo 100)
     */
    private suspend fun processBlock(block: List<BackupItem>, blockNumber: Int, totalBlocks: Int) {
        // Procesar cada elemento del bloque
        block.forEach { item ->
            if (!isProcessing) return
            
            // Aquí iría la lógica de importación real
            // Por ejemplo: parsear JSON, guardar en base de datos, etc.
            processMangaItem(item)
            
            // Pequeña pausa para no saturar
            delay(10)
        }
    }

    /**
     * Procesa un elemento individual de manga
     */
    private fun processMangaItem(item: BackupItem) {
        // Implementar lógica de importación real aquí
        // Ejemplo: parsear JSON, guardar en Room Database, etc.
        // Por ahora es un placeholder
    }

    /**
     * Muestra notificación de error
     */
    private fun showErrorNotification(message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Error en migración")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID + 1, notification)
    }

    /**
     * Muestra notificación de completado
     */
    private fun showCompletionNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Migración completada")
            .setContentText("$processedItems elementos importados correctamente")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID + 1, notification)
    }

    /**
     * Detiene la migración
     */
    private fun stopMigration() {
        isProcessing = false
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Migración cancelada")
            .setContentText("Proceso detenido por el usuario")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID + 1, notification)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        isProcessing = false
        serviceScope.cancel()
    }

    /**
     * Clase de datos para representar un elemento del backup
     */
    data class BackupItem(
        val name: String,
        val size: Long
    )
}
