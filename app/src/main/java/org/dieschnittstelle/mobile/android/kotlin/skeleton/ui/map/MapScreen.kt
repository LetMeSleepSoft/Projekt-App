package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list.TopAppBar
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

// "https://tiles.openfreemap.org/styles/liberty"
@Composable
fun MapScreen(
    onNavigateToList: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {

    val coroutineScope = rememberCoroutineScope()
    val markers by viewModel.markers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet() {
                Row(
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Navigation",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Box{
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.close()
                                } },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = ""
                            )
                        }
                    }
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "MediaItems",
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        onNavigateToList()
                    }
                )
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Map",
                            modifier = Modifier.padding(16.dp)
                        )
                    },
                    selected = false,
                    onClick = { }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    scope = coroutineScope,
                    drawerstate = drawerState,
                )
            }
        ) { innerPadding ->
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                MapLibreView(
                    modifier = Modifier.padding(innerPadding),
                    styleUrl = "https://tiles.openfreemap.org/styles/liberty",
                    markers = markers,
                    initialPosition = markers.firstOrNull()?.position
                        ?: LatLng(52.520008, 13.404954),
                    initialZoom = if (markers.isNotEmpty()) 12.0 else 10.0,
                    onMarkerClick = onNavigateToDetails
                )
            }
        }
    }
}

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    styleUrl: String = "https://tiles.openfreemap.org/styles/liberty",
    markers: List<MarkerData> = emptyList(),
    initialPosition: LatLng = LatLng(52.520008, 13.404954),
    initialZoom: Double = 10.0,
    onMarkerClick: (Long) -> Unit = {}
    
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.getMapAsync { map ->
                map.setStyle(styleUrl) {
                    map.clear()

                    // Marker mit Zuordnung speichern
                    val markerMap = mutableMapOf<org.maplibre.android.annotations.Marker, MarkerData>()

                    markers.forEach { markerData ->
                        val marker = map.addMarker(
                            MarkerOptions()
                                .position(markerData.position)
                                .title(markerData.title)
                                .snippet("Klicken für Details")
                        )
                        markerMap[marker] = markerData
                    }

                    // Info-Fenster Click
                    map.setOnInfoWindowClickListener { clickedMarker ->
                        clickedMarker.let { marker ->
                            markerMap[marker]?.let { markerData ->
                                onMarkerClick(markerData.id)
                            }
                        }
                        true
                    }

                    map.cameraPosition = CameraPosition.Builder()
                        .target(initialPosition)
                        .zoom(initialZoom)
                        .build()
                }
            }
        }
    )
}

data class MarkerData(
    val position: LatLng,
    val title: String,
    val id: Long,
)
