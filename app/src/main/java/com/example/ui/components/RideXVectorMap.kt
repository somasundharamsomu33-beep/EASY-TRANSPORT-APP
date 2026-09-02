package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DriverEntity
import com.example.data.model.LocationPoint
import com.example.data.model.RideStatus
import com.example.data.model.VehicleCategory
import com.example.ui.theme.RideXAmber
import com.example.ui.theme.RideXCyan
import com.example.ui.theme.RideXEmerald
import com.example.ui.theme.RideXRose
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RideXVectorMap(
    modifier: Modifier = Modifier,
    pickupLocation: LocationPoint?,
    dropLocation: LocationPoint?,
    routePoints: List<LocationPoint> = emptyList(),
    nearbyDrivers: List<DriverEntity> = emptyList(),
    activeDriverPoint: LocationPoint? = null,
    activeDriverCategory: VehicleCategory? = null,
    rideStatus: RideStatus? = null,
    routeProgressPercent: Float = 0f,
    onCenterLocation: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()

    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing animations
    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Dynamic wave animation for road navigation
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash_phase"
    )

    val mapBgColor = if (isDark) Color(0xFF141218) else Color(0xFFFEF7FF)
    val roadColor = if (isDark) Color(0xFF211F26) else Color(0xFFFFFFFF)
    val arterialRoadColor = if (isDark) Color(0xFF36343B) else Color(0xFFE6E0E9)
    val blockColor = if (isDark) Color(0xFF1D1B20) else Color(0xFFF7F2FA)
    val waterColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE0F2FE)
    val parkColor = if (isDark) Color(0xFF142921) else Color(0xFFE8F5E9)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(mapBgColor)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.5f)
                    panOffsetX += pan.x
                    panOffsetY += pan.y
                }
            }
            .testTag("ridex_vector_map")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f + panOffsetX
            val centerY = height / 2f + panOffsetY

            // 1. Draw City Grid & Landmarks
            drawCityBlocks(
                centerX, centerY, width, height, zoomScale,
                blockColor, roadColor, arterialRoadColor, waterColor, parkColor
            )

            // 2. Coordinate Mapping Setup
            // Chennai reference bounds: lat 12.96 to 13.12, lng 80.15 to 80.30
            fun projectLat(lat: Double): Float {
                val relY = (13.0827 - lat) * 4500f * zoomScale
                return centerY + relY.toFloat()
            }

            fun projectLng(lng: Double): Float {
                val relX = (lng - 80.2707) * 4500f * zoomScale
                return centerX + relX.toFloat()
            }

            // 3. Draw Route Polyline
            if (routePoints.size >= 2) {
                val routePath = Path()
                val startScreen = Offset(projectLng(routePoints.first().longitude), projectLat(routePoints.first().latitude))
                routePath.moveTo(startScreen.x, startScreen.y)

                for (i in 1 until routePoints.size) {
                    val p = routePoints[i]
                    val screenPt = Offset(projectLng(p.longitude), projectLat(p.latitude))
                    routePath.lineTo(screenPt.x, screenPt.y)
                }

                // Outer neon glow stroke
                drawPath(
                    path = routePath,
                    color = RideXCyan.copy(alpha = 0.35f),
                    style = Stroke(width = 14f * zoomScale)
                )

                // Main route line with animated dash
                drawPath(
                    path = routePath,
                    color = RideXCyan,
                    style = Stroke(
                        width = 6f * zoomScale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), dashPhase)
                    )
                )
            }

            // 4. Draw Nearby Available Drivers (when idle / searching)
            if (rideStatus == null || rideStatus == RideStatus.REQUESTED || rideStatus == RideStatus.SEARCHING_DRIVER) {
                nearbyDrivers.forEach { driver ->
                    val dX = projectLng(driver.currentLng)
                    val dY = projectLat(driver.currentLat)

                    // Driver vehicle aura & icon
                    drawCircle(
                        color = RideXAmber.copy(alpha = 0.2f),
                        radius = 16f * zoomScale,
                        center = Offset(dX, dY)
                    )
                    drawCircle(
                        color = RideXAmber,
                        radius = 8f * zoomScale,
                        center = Offset(dX, dY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.5f * zoomScale,
                        center = Offset(dX, dY)
                    )
                }
            }

            // 5. Draw Pickup Marker
            pickupLocation?.let { pickup ->
                val pX = projectLng(pickup.longitude)
                val pY = projectLat(pickup.latitude)

                // Pulsing radar ring
                drawCircle(
                    color = RideXEmerald.copy(alpha = pulseAlpha),
                    radius = pulseRadius * zoomScale,
                    center = Offset(pX, pY)
                )
                // Outer ring
                drawCircle(
                    color = RideXEmerald,
                    radius = 9f * zoomScale,
                    center = Offset(pX, pY)
                )
                // Center core
                drawCircle(
                    color = Color.White,
                    radius = 4.5f * zoomScale,
                    center = Offset(pX, pY)
                )
            }

            // 6. Draw Drop Destination Marker
            dropLocation?.let { drop ->
                val dX = projectLng(drop.longitude)
                val dY = projectLat(drop.latitude)

                // Outer red ring
                drawCircle(
                    color = RideXRose.copy(alpha = 0.3f),
                    radius = 14f * zoomScale,
                    center = Offset(dX, dY)
                )
                drawCircle(
                    color = RideXRose,
                    radius = 9f * zoomScale,
                    center = Offset(dX, dY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.5f * zoomScale,
                    center = Offset(dX, dY)
                )
            }

            // 7. Draw Active Driver Live Tracking Marker
            activeDriverPoint?.let { driverPt ->
                val adX = projectLng(driverPt.longitude)
                val adY = projectLat(driverPt.latitude)

                // Driver pulse aura
                drawCircle(
                    color = RideXCyan.copy(alpha = pulseAlpha * 0.7f),
                    radius = (pulseRadius + 8f) * zoomScale,
                    center = Offset(adX, adY)
                )
                drawCircle(
                    color = Color(0xFF0F172A),
                    radius = 12f * zoomScale,
                    center = Offset(adX, adY)
                )
                drawCircle(
                    color = RideXCyan,
                    radius = 8f * zoomScale,
                    center = Offset(adX, adY)
                )
                // Heading arrow indicator
                val arrowPath = Path().apply {
                    moveTo(adX, adY - (10f * zoomScale))
                    lineTo(adX - (5f * zoomScale), adY + (4f * zoomScale))
                    lineTo(adX, adY + (2f * zoomScale))
                    lineTo(adX + (5f * zoomScale), adY + (4f * zoomScale))
                    close()
                }
                drawPath(arrowPath, color = Color.White)
            }
        }

        // Map Floating Controls (Zoom in, Zoom out, My Location Center)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmallFloatingActionButton(
                onClick = { zoomScale = (zoomScale * 1.2f).coerceAtMost(3.5f) },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("map_zoom_in")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }

            SmallFloatingActionButton(
                onClick = { zoomScale = (zoomScale / 1.2f).coerceAtLeast(0.6f) },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("map_zoom_out")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            FloatingActionButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                    zoomScale = 1.0f
                    onCenterLocation()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("map_recenter")
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter Location", modifier = Modifier.size(22.dp))
            }
        }

        // Live GPS / Telemetry badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            tonalElevation = 4.dp
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "GPS High Accuracy • Live",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = RideXEmerald,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

private fun DrawScope.drawCityBlocks(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    scale: Float,
    blockColor: Color,
    roadColor: Color,
    arterialRoadColor: Color,
    waterColor: Color,
    parkColor: Color
) {
    val gridSize = 140f * scale

    // Draw Coastline / Bay of Bengal on East side
    val waterPath = Path().apply {
        val waterStartX = centerX + (420f * scale)
        moveTo(waterStartX, 0f)
        lineTo(width, 0f)
        lineTo(width, height)
        lineTo(waterStartX - 80f, height)
        cubicTo(
            waterStartX + 50f, height * 0.6f,
            waterStartX - 60f, height * 0.3f,
            waterStartX, 0f
        )
        close()
    }
    drawPath(waterPath, color = waterColor)

    // Draw City Parks / Nature reserves
    drawRoundRect(
        color = parkColor,
        topLeft = Offset(centerX - (260f * scale), centerY - (180f * scale)),
        size = androidx.compose.ui.geometry.Size(180f * scale, 120f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )

    drawRoundRect(
        color = parkColor,
        topLeft = Offset(centerX + (80f * scale), centerY + (160f * scale)),
        size = androidx.compose.ui.geometry.Size(140f * scale, 90f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
    )

    // Draw Urban Block Grids
    val startX = (centerX % gridSize) - gridSize * 2
    val startY = (centerY % gridSize) - gridSize * 2

    var x = startX
    while (x < width + gridSize * 2) {
        var y = startY
        while (y < height + gridSize * 2) {
            drawRoundRect(
                color = blockColor,
                topLeft = Offset(x + (8f * scale), y + (8f * scale)),
                size = androidx.compose.ui.geometry.Size(gridSize - (16f * scale), gridSize - (16f * scale)),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale, 6f * scale)
            )
            y += gridSize
        }
        x += gridSize
    }

    // Draw Standard Roads
    var rx = startX
    while (rx < width + gridSize * 2) {
        drawLine(
            color = roadColor,
            start = Offset(rx, 0f),
            end = Offset(rx, height),
            strokeWidth = 5f * scale
        )
        rx += gridSize
    }

    var ry = startY
    while (ry < height + gridSize * 2) {
        drawLine(
            color = roadColor,
            start = Offset(0f, ry),
            end = Offset(width, ry),
            strokeWidth = 5f * scale
        )
        ry += gridSize
    }

    // Major Arterial Highways
    drawLine(
        color = arterialRoadColor,
        start = Offset(0f, centerY),
        end = Offset(width, centerY),
        strokeWidth = 10f * scale
    )
    drawLine(
        color = arterialRoadColor,
        start = Offset(centerX, 0f),
        end = Offset(centerX, height),
        strokeWidth = 10f * scale
    )
    drawLine(
        color = arterialRoadColor.copy(alpha = 0.7f),
        start = Offset(0f, 0f),
        end = Offset(width, height),
        strokeWidth = 8f * scale
    )
}
