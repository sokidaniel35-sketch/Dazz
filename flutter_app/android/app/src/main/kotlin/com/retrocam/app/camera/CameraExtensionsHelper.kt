package com.retrocam.app.camera

import android.content.Context
import android.hardware.camera2.CameraExtensionCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import androidx.camera.extensions.ExtensionMode

/**
 * Utilitários para as Camera Extensions (vendor extensions) do dispositivo.
 *
 * Duas camadas de consulta:
 *  1. CameraX (ExtensionsManager) — usada para realmente ligar o modo no pipeline.
 *  2. Camera2 (CameraExtensionCharacteristics, API 31+) — consulta crua do que o
 *     fabricante anuncia; inclui IDs que o CameraX ainda não conhece (extensões
 *     definidas pelo fabricante no Android 17). Aparecem como "vendor_<id>".
 */
internal object CameraExtensionsHelper {
    private const val TAG = "CameraExtensions"

    /** Nome (Dart) -> ExtensionMode do CameraX. */
    val modeByName: Map<String, Int> = linkedMapOf(
        "none" to ExtensionMode.NONE,
        "auto" to ExtensionMode.AUTO,
        "hdr" to ExtensionMode.HDR,
        "night" to ExtensionMode.NIGHT,
        "bokeh" to ExtensionMode.BOKEH,
        "face_retouch" to ExtensionMode.FACE_RETOUCH
    )

    fun nameOf(mode: Int): String =
        modeByName.entries.firstOrNull { it.value == mode }?.key ?: "none"

    /** IDs crus do Camera2 suportados pela câmera [cameraId]; vazio se API < 31 ou erro. */
    fun camera2SupportedExtensions(context: Context, cameraId: String): List<Int> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return emptyList()
        return try {
            val mgr = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            mgr.getCameraExtensionCharacteristics(cameraId).supportedExtensions
        } catch (e: Exception) {
            Log.w(TAG, "camera2SupportedExtensions($cameraId) failed: ${e.message}")
            emptyList()
        }
    }

    /** Nome legível de um ID de extensão do Camera2; IDs desconhecidos viram vendor_<id>. */
    @Suppress("DEPRECATION")
    fun camera2Name(id: Int): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return "vendor_$id"
        return when (id) {
            CameraExtensionCharacteristics.EXTENSION_AUTOMATIC -> "auto"
            CameraExtensionCharacteristics.EXTENSION_HDR -> "hdr"
            CameraExtensionCharacteristics.EXTENSION_NIGHT -> "night"
            CameraExtensionCharacteristics.EXTENSION_BOKEH -> "bokeh"
            CameraExtensionCharacteristics.EXTENSION_FACE_RETOUCH -> "face_retouch"
            else -> "vendor_$id"
        }
    }
}
