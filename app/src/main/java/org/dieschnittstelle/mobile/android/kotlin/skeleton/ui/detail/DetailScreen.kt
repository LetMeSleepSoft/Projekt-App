package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.detail

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toDto
import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.MediaItemDTO
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list.DeleteDialog
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list.DeleteDialogUiState
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list.MinimalDialogUiState


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun DetailScreen(
    mediaItemId: String,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteDialogUiState = viewModel.deleteDetailDialogUiState

    var isRemote by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(mediaItemId) {
        if (mediaItemId.contains("-")) {
            isRemote = true
            viewModel.getRemoteItem(mediaItemId)
        } else{
            Log.i("DETAILS SCREEN ID", "ID: ${mediaItemId}")
            viewModel.getItemById(mediaItemId)
        }
    }

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
                deleteDetailDialogUiState = deleteDialogUiState
            )
        }
    ) { innerPadding ->
        DetailBody(
            mediaItem = uiState.mediaItem,
            modifier = Modifier.padding(innerPadding)
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


@Composable
fun DetailBody(
    mediaItem: MediaItemDetails,
    modifier: Modifier = Modifier
) {
    val bitmap = BitmapFactory.decodeByteArray(
        mediaItem.src,
        0,
        mediaItem.src!!.size
    )

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsTopAppBar(
    onChangeVisibility: (Boolean, MediaItemDetails) -> Unit,
    deleteDetailDialogUiState: DeleteDetailDialogUiState,
    mediaItemDetails: MediaItemDetails,
    onDelete: (MediaItemDetails) -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = mediaItemDetails.title
            )
        },
        navigationIcon = {
            IconButton(onClick = {}) {
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


