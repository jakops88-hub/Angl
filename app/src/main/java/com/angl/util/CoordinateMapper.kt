package com.angl.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

/**
 * CoordinateMapper handles the critical task of translating coordinates
 * between camera sensor space and screen/UI space.
 * 
 * This is the most complex part of AR overlay implementations because:
 * 1. Camera sensor typically outputs 4:3 or 16:9 aspect ratio
 * 2. Screen displays are often 18:9, 19.5:9, or 20:9 aspect ratio
 * 3. The camera preview may be scaled, cropped, or letterboxed
 * 4. Front camera requires horizontal mirroring
 * 5. Device rotation affects coordinate systems
 * 
 * Without proper coordinate mapping, AR overlays will be misaligned
 * and appear to "float" away from the detected features.
 */
object CoordinateMapper {

    /**
     * Maps a point from camera source coordinates to screen/UI coordinates.
     * 
     * This function handles:
     * - Aspect ratio differences between source and target
     * - Scaling to fit the preview into the screen
     * - Optional mirroring for front camera
     * 
     * The mapping assumes SCALE_TYPE_FILL_CENTER behavior where the preview
     * is scaled to fill the target while maintaining aspect ratio, with
     * cropping if necessary.
     * 
     * @param x X coordinate in source (camera) space
     * @param y Y coordinate in source (camera) space
     * @param sourceSize Size of the camera image (e.g., 640x480 or 1920x1080)
     * @param targetSize Size of the screen/canvas where preview is displayed
     * @param isMirrored True if using front camera (requires horizontal flip)
     * @return Offset representing the mapped coordinate in target space
     */
    fun mapPoint(
        x: Float,
        y: Float,
        sourceSize: IntSize,
        targetSize: IntSize,
        isMirrored: Boolean = false
    ): Offset {
        // Handle edge case: invalid sizes
        if (sourceSize.width <= 0 || sourceSize.height <= 0 ||
            targetSize.width <= 0 || targetSize.height <= 0) {
            return Offset(0f, 0f)
        }

        // Calculate aspect ratios
        val sourceAspect = sourceSize.width.toFloat() / sourceSize.height.toFloat()
        val targetAspect = targetSize.width.toFloat() / targetSize.height.toFloat()

        // Calculate scale factor
        // We use FILL_CENTER logic: scale to fill the target, cropping if needed
        val scale = if (sourceAspect > targetAspect) {
            // Source is wider - scale based on height
            targetSize.height.toFloat() / sourceSize.height.toFloat()
        } else {
            // Source is taller or same - scale based on width
            targetSize.width.toFloat() / sourceSize.width.toFloat()
        }

        // Calculate the scaled source dimensions
        val scaledSourceWidth = sourceSize.width * scale
        val scaledSourceHeight = sourceSize.height * scale

        // Calculate offsets to center the scaled image
        val offsetX = (targetSize.width - scaledSourceWidth) / 2f
        val offsetY = (targetSize.height - scaledSourceHeight) / 2f

        // Apply mirroring for front camera
        val sourceX = if (isMirrored) {
            sourceSize.width - x
        } else {
            x
        }

        // Map the coordinates
        val mappedX = (sourceX * scale) + offsetX
        val mappedY = (y * scale) + offsetY

        // Clamp to target bounds to prevent drawing outside screen area
        // Note: This ensures UI elements stay within visible bounds but may
        // hide landmarks that are legitimately outside the frame during certain
        // camera movements. For visualization purposes, clamping is preferred
        // to avoid rendering artifacts. If you need to detect off-screen landmarks,
        // check the original coordinates before clamping.
        val clampedX = mappedX.coerceIn(0f, targetSize.width.toFloat())
        val clampedY = mappedY.coerceIn(0f, targetSize.height.toFloat())

        return Offset(clampedX, clampedY)
    }

    /**
     * Maps a point from camera source coordinates to screen coordinates
     * using standard Size objects instead of IntSize.
     * 
     * This is a convenience overload for when you're working with
     * float-based Size objects.
     * 
     * @param x X coordinate in source space
     * @param y Y coordinate in source space
     * @param sourceSize Size of the camera image
     * @param targetSize Size of the screen/canvas
     * @param isMirrored True if using front camera
     * @return Offset in target space
     */
    fun mapPoint(
        x: Float,
        y: Float,
        sourceSize: android.util.Size,
        targetSize: android.util.Size,
        isMirrored: Boolean = false
    ): Offset {
        return mapPoint(
            x = x,
            y = y,
            sourceSize = IntSize(sourceSize.width, sourceSize.height),
            targetSize = IntSize(targetSize.width, targetSize.height),
            isMirrored = isMirrored
        )
    }

    /**
     * Maps multiple points in batch for efficiency.
     * 
     * This is useful when mapping pose landmarks where you need to
     * transform 33 points (full body pose) or 11+ points (upper body).
     * 
     * @param points List of points to map
     * @param sourceSize Camera image size
     * @param targetSize Screen size
     * @param isMirrored Front camera flag
     * @return List of mapped offsets
     */
    fun mapPoints(
        points: List<Pair<Float, Float>>,
        sourceSize: IntSize,
        targetSize: IntSize,
        isMirrored: Boolean = false
    ): List<Offset> {
        return points.map { (x, y) ->
            mapPoint(x, y, sourceSize, targetSize, isMirrored)
        }
    }

    /**
     * Calculates the visible region of the camera preview after scaling.
     * 
     * When the camera preview is scaled to fill the screen, parts may be
     * cropped. This function returns the region of the source image that
     * is actually visible on screen.
     * 
     * This is useful for:
     * - Determining if a landmark is within the visible area
     * - Adjusting overlay positions
     * - Clipping drawing operations
     * 
     * @param sourceSize Camera image size
     * @param targetSize Screen size
     * @return Pair of (visibleWidth, visibleHeight) in source coordinates
     */
    fun calculateVisibleRegion(
        sourceSize: IntSize,
        targetSize: IntSize
    ): Pair<Float, Float> {
        if (sourceSize.width <= 0 || sourceSize.height <= 0 ||
            targetSize.width <= 0 || targetSize.height <= 0) {
            return Pair(0f, 0f)
        }

        val sourceAspect = sourceSize.width.toFloat() / sourceSize.height.toFloat()
        val targetAspect = targetSize.width.toFloat() / targetSize.height.toFloat()

        return if (sourceAspect > targetAspect) {
            // Source is wider - height is fully visible, width is cropped
            val visibleWidth = sourceSize.height * targetAspect
            Pair(visibleWidth, sourceSize.height.toFloat())
        } else {
            // Source is taller - width is fully visible, height is cropped
            val visibleHeight = sourceSize.width / targetAspect
            Pair(sourceSize.width.toFloat(), visibleHeight)
        }
    }

    /**
     * Determines the scale factor used when fitting the camera preview
     * into the target area using FILL_CENTER logic.
     * 
     * @param sourceSize Camera image size
     * @param targetSize Screen size
     * @return Scale factor applied to source coordinates
     */
    fun calculateScaleFactor(
        sourceSize: IntSize,
        targetSize: IntSize
    ): Float {
        if (sourceSize.width <= 0 || sourceSize.height <= 0 ||
            targetSize.width <= 0 || targetSize.height <= 0) {
            return 1f
        }

        val sourceAspect = sourceSize.width.toFloat() / sourceSize.height.toFloat()
        val targetAspect = targetSize.width.toFloat() / targetSize.height.toFloat()

        return if (sourceAspect > targetAspect) {
            targetSize.height.toFloat() / sourceSize.height.toFloat()
        } else {
            targetSize.width.toFloat() / sourceSize.width.toFloat()
        }
    }

    /**
     * Inverse mapping: converts screen coordinates back to camera coordinates.
     * 
     * This is useful for:
     * - Touch event handling (user taps on screen, need camera coords)
     * - Interactive AR features
     * - Gesture recognition
     * 
     * @param screenX X coordinate on screen
     * @param screenY Y coordinate on screen
     * @param sourceSize Camera image size
     * @param targetSize Screen size
     * @param isMirrored Front camera flag
     * @return Offset in camera coordinate space, or null if out of bounds
     */
    fun screenToCamera(
        screenX: Float,
        screenY: Float,
        sourceSize: IntSize,
        targetSize: IntSize,
        isMirrored: Boolean = false
    ): Offset? {
        if (sourceSize.width <= 0 || sourceSize.height <= 0 ||
            targetSize.width <= 0 || targetSize.height <= 0) {
            return null
        }

        val sourceAspect = sourceSize.width.toFloat() / sourceSize.height.toFloat()
        val targetAspect = targetSize.width.toFloat() / targetSize.height.toFloat()

        val scale = if (sourceAspect > targetAspect) {
            targetSize.height.toFloat() / sourceSize.height.toFloat()
        } else {
            targetSize.width.toFloat() / sourceSize.width.toFloat()
        }

        val scaledSourceWidth = sourceSize.width * scale
        val scaledSourceHeight = sourceSize.height * scale
        val offsetX = (targetSize.width - scaledSourceWidth) / 2f
        val offsetY = (targetSize.height - scaledSourceHeight) / 2f

        // Reverse the mapping
        val cameraX = (screenX - offsetX) / scale
        val cameraY = (screenY - offsetY) / scale

        // Check if within source bounds
        if (cameraX < 0 || cameraX > sourceSize.width ||
            cameraY < 0 || cameraY > sourceSize.height) {
            return null
        }

        // Apply mirroring
        val finalX = if (isMirrored) {
            sourceSize.width - cameraX
        } else {
            cameraX
        }

        return Offset(finalX, cameraY)
    }
}
