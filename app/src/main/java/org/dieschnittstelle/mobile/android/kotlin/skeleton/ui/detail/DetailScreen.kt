package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.detail

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toDto
import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.MediaItemDTO
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list.DeleteDialog
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list.DeleteDialogUiState
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list.MinimalDialogUiState
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.map.GpsCoordinates
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.map.MapLibreView
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.map.MarkerData
import org.maplibre.android.geometry.LatLng


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun DetailScreen(
    mediaItemId: String,
    onNavigateBack: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToMap: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteDialogUiState = viewModel.deleteDetailDialogUiState

    var isRemote by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(mediaItemId) {
        if (mediaItemId.contains("-")) {
            isRemote = true
            viewModel.getRemoteItem(mediaItemId)
        } else{
            Log.i("DETAILS SCREEN ID", "ID: ${mediaItemId}")
            viewModel.getItemById(mediaItemId)
        }
    }
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
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        onNavigateToMap()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                DetailsTopAppBar(
                    mediaItemDetails = uiState.mediaItem,
                    onDelete = { item ->
                        coroutineScope.launch {
                            if (!isRemote) {
                                viewModel.deleteItem(item)
                            } else {
                                viewModel.deleteRemoteItem(item.toDto())
                            }
                        }
                    },
                    onChangeVisibility = viewModel::changeDeleteDialogUiState,
                    deleteDetailDialogUiState = deleteDialogUiState,
                    scope = coroutineScope,
                    drawerState = drawerState,
                )
            }
        ) { innerPadding ->
            DetailBody(
                mediaItem = uiState.mediaItem,
                modifier = Modifier.padding(innerPadding),
                onNavigateBack = onNavigateBack,
                extractGps = viewModel::extractGpsFromImage,
            )

            if (deleteDialogUiState.isVisible) {
                SimpleDeleteDialog(
                    mediaItem = uiState.mediaItem,
                    onDismiss = {},
                    onDelete = { item ->
                        coroutineScope.launch {
                            if (!isRemote) {
                                viewModel.deleteItem(item)
                            } else {
                                viewModel.deleteRemoteItem(item.toDto())
                            }
                        }
                    },
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}


@Composable
fun DetailBody(
    mediaItem: MediaItemDetails,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    extractGps: (ByteArray) -> GpsCoordinates?
) {
    val bitmap = BitmapFactory.decodeByteArray(
        mediaItem.src,
        0,
        mediaItem.src!!.size
    )

    val listTest = mutableListOf<MarkerData?>()
    val test = extractGps(mediaItem.src)?.let { (lat, lng) ->
        MarkerData(
            position = LatLng(lat, lng),
            title = "Item",
            id = 0,
        )
    }
    listTest.add(test)

    val lat = test?.position?.latitude
    val lng = test?.position?.longitude

    Log.i("DETAILSCREEN COORDS","COORDS: ${test?.position?.longitude} + ${test?.position?.latitude}")

    Column() {
        Box(
            modifier = modifier.weight(1f)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize()
                )
            } else {
                Text("FEHLER")
            }
        }
        IconButton(
            onClick = {
                onNavigateBack()
            }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = ""
            )
        }
        Box(
           modifier = modifier.weight(0.5f)
        ) {
            MapLibreView(
                styleUrl = "https://tiles.openfreemap.org/styles/liberty",
                markers = listTest as List<MarkerData>,
                initialPosition = listTest.firstOrNull()?.position
                    ?: LatLng(52.520008, 13.404954)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsTopAppBar(
    onChangeVisibility: (Boolean, MediaItemDetails) -> Unit,
    deleteDetailDialogUiState: DeleteDetailDialogUiState,
    mediaItemDetails: MediaItemDetails,
    onDelete: (MediaItemDetails) -> Unit,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    TopAppBar(
        title = {
            Text(
                text = mediaItemDetails.title
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    drawerState.open()
                }
                Log.i("DETAILSVIEW: ", "BUTTON CLICKED")
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = ""
                )
            }
        },
        actions = {
            IconButton(onClick = {
                onChangeVisibility(deleteDetailDialogUiState.isVisible, mediaItemDetails)
            }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = ""
                )
            }
        }
    )
}

@Composable
fun SimpleDeleteDialog(
    mediaItem: MediaItemDetails,
    onDismiss: () -> Unit,
    onDelete: (MediaItemDetails) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }

    if (isVisible) {
        DeleteDialog(
            mediaItem = mediaItem,
            deleteDialogUiState = DeleteDialogUiState(isDeleteDialogVisible = true),
            minimalDialogUiState = MinimalDialogUiState(isMinimalDialogVisible = false),
            onDismiss = { _, _ ->
                isVisible = false
                onDismiss()
            },
            onDismissMinimalDialog = { _, _ -> },
            onDelete = {
                onDelete(mediaItem)
                onNavigateBack()
            },
            modifier = modifier
        )
    }
}


