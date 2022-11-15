/*
 * SPDX-FileCopyrightText: 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture.utils

import android.hardware.camera2.CameraCharacteristics
import android.util.SizeF
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.CameraInfo
import androidx.camera.video.QualitySelector
import org.lineageos.aperture.getSupportedModes
import org.lineageos.aperture.physicalCameraIds
import kotlin.reflect.safeCast
import kotlin.runCatching

/**
 * Class representing a device camera
 */
@androidx.camera.camera2.interop.ExperimentalCamera2Interop
class Camera(cameraInfo: CameraInfo, cameraManager: CameraManager) {
    val cameraSelector = cameraInfo.cameraSelector

    val camera2CameraInfo = Camera2CameraInfo.from(cameraInfo)
    val cameraId = camera2CameraInfo.cameraId

    val cameraFacing =
        when (camera2CameraInfo.getCameraCharacteristic(CameraCharacteristics.LENS_FACING)) {
            CameraCharacteristics.LENS_FACING_FRONT -> CameraFacing.FRONT
            CameraCharacteristics.LENS_FACING_BACK -> CameraFacing.BACK
            CameraCharacteristics.LENS_FACING_EXTERNAL -> CameraFacing.EXTERNAL
            else -> CameraFacing.UNKNOWN
        }

    val exposureCompensationRange = cameraInfo.exposureState.exposureCompensationRange
    val hasFlashUnit = cameraInfo.hasFlashUnit()

    val physicalCameraIds = camera2CameraInfo.physicalCameraIds
    val isLogical = physicalCameraIds.isNotEmpty()

    val focalLengths = mutableSetOf<Float>().apply {
        addAll(camera2CameraInfo.getCameraCharacteristic(
            CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
        )?.toSet() ?: setOf())

        for (physicalCameraId in physicalCameraIds) {
            runCatching {
                val camera2CameraCharacteristics =
                    cameraManager.camera2CameraManager.getCameraCharacteristics(physicalCameraId)
                val physicalFocalLengths = camera2CameraCharacteristics.get(
                    CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
                ) ?: FloatArray(0)
                for (focalLength in physicalFocalLengths) {
                    add(focalLength)
                }
            }
        }
    }.toSet()
    val sensorSize = camera2CameraInfo.getCameraCharacteristic(
        CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE
    )

    val mm35FocalLengths = sensorSize?.let { sensorSize ->
        focalLengths.map { getMm35FocalLength(it, sensorSize) }
    }
    var zoomRatio = 1f

    val supportedVideoQualities = QualitySelector.getSupportedQualities(cameraInfo).reversed()
    val supportedVideoFramerates: List<Framerate> = mutableListOf(Framerate.FPS_AUTO).apply {
        camera2CameraInfo.getCameraCharacteristic(
            CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES
        )?.let {
            addAll(it.mapNotNull { range -> Framerate.fromRange(range) }.distinct().sorted())
        }
    }
    val supportsVideoRecording = supportedVideoQualities.isNotEmpty()

    val supportedExtensionModes = cameraManager.extensionsManager.getSupportedModes(cameraSelector)

    override fun equals(other: Any?): Boolean {
        val camera = this::class.safeCast(other) ?: return false
        return this.cameraId == camera.cameraId
    }

    override fun hashCode(): Int {
        return this::class.qualifiedName.hashCode() + cameraId.hashCode()
    }

    fun supportsExtensionMode(extensionMode: Int): Boolean {
        return supportedExtensionModes.contains(extensionMode)
    }

    fun supportsCameraMode(cameraMode: CameraMode): Boolean {
        return when (cameraMode) {
            CameraMode.VIDEO -> supportsVideoRecording
            else -> true
        }
    }

    companion object {
        fun getMm35FocalLength(focalLength: Float, sensorSize: SizeF): Float {
            return (36.0f / sensorSize.width) * focalLength
        }
    }
}