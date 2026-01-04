package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.dieschnittstelle.mobile.android.kotlin.skeleton.R
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toMediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toMediaItemDetails


@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetails: (Long) -> Unit,
    viewModel: MediaItemViewModel = hiltViewModel()
){
    val coroutineScope = rememberCoroutineScope()
    val mediaItemListUiState by viewModel.mediaItemListUiState.collectAsState()
    val context: Context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(end = 16.dp, bottom = 16.dp),
                onClick = {
                    viewModel.changeBottomSheetUiState(
                        viewModel.bottomSheetUiState.isBottomSheetVisible
                    )
                },
            ) {
                Icon(Icons.Filled.Add, "Floating action Button")
            }
        }
    ) { innerPadding ->
        ListScreenBody(
            bottomSheetUiState = viewModel.bottomSheetUiState,
            alertDialogState = viewModel.alertDialogUiState,
            onShowBottomSheet = viewModel::changeBottomSheetUiState,
            onShowEditBottomSheet = viewModel::transferMediaItemToBottomUiState,
            onSheetItemValueChange = viewModel::updateBottomSheetUiState,
            mediaItems = mediaItemListUiState.mediaItems,
            modifier = Modifier.padding(innerPadding),
            onSave = {
                coroutineScope.launch {
                    viewModel.saveMediaItem()
                    viewModel.changeBottomSheetUiState(
                        viewModel.bottomSheetUiState.isBottomSheetVisible
                    )
                }
            },
            onDelete = { item ->
              coroutineScope.launch {
                  viewModel.deleteItem(item)
              }
            },
            onUpdate = { item ->
                coroutineScope.launch {
                    viewModel.updateItem(item)
                }
            },
            onImagePicked = viewModel::uriToByteArray,
            context = context,
            onDismiss = viewModel::changeAlertDialogUiState,
            minimalDialogUiState = viewModel.minimalDialogUiState,
            deleteDialogUiState = viewModel.deleteDialogUiState,
            onShowMinimalDialog = viewModel::changeMinimalDialogUiState,
            onShowDeleteDialog = viewModel::changeDeleteDialogUiState,
            onNavigateToDetails = onNavigateToDetails
        )
    }
}

@Composable
fun ListScreenBody(
    bottomSheetUiState: BottomSheetUiState,
    alertDialogState: AlertDialogUiState,
    minimalDialogUiState: MinimalDialogUiState,
    deleteDialogUiState: DeleteDialogUiState,
    onShowBottomSheet: (Boolean) -> Unit,
    onShowEditBottomSheet: (MediaItemDetails) -> Unit,
    onSheetItemValueChange: (MediaItemDetails) -> Unit,
    mediaItems: List<MediaItem>,
    onSave: () -> Unit,
    onDelete: (MediaItem) -> Unit,
    onUpdate: (MediaItem) -> Unit,
    onImagePicked: (Context, Uri) -> ByteArray?,
    onDismiss: (Boolean) -> Unit,
    onShowMinimalDialog: (Boolean, MediaItemDetails) -> Unit,
    onShowDeleteDialog: (Boolean, MediaItemDetails) -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier
    ) {
        items(items = mediaItems) { item ->
            ListItem(
                mediaItem = item,
                minimalDialogUiState = minimalDialogUiState,
                onChangeMinimalDialogState = onShowMinimalDialog,
                onNavigateToDetails = onNavigateToDetails
            )
        }
    }

    if (bottomSheetUiState.isBottomSheetVisible) {
        BottomModal(
            bottomSheetUiState = bottomSheetUiState,
            alertDialogUiState = alertDialogState,
            onShowBottomSheet = onShowBottomSheet,
            onSheetItemValueChange = onSheetItemValueChange,
            mediaItem = bottomSheetUiState.mediaItemDetails,
            onSave = onSave,
            context = context,
            onImagePicked = onImagePicked,
            onDelete = onDelete,
            onUpdate = onUpdate
        )
    }

    if (alertDialogState.isDialogVisible){
        EntryCheckAlertDialog(
            alertDialogUiState = alertDialogState,
            bottomSheetUiState = bottomSheetUiState,
            onDismiss = onDismiss,
            onConfirm = onShowBottomSheet,
        )
    }

    if (minimalDialogUiState.isMinimalDialogVisible) {
        MinimalDialog(
            mediaItem = minimalDialogUiState.mediaItem,
            minimalDialogUiState = minimalDialogUiState,
            bottomSheetUiState = bottomSheetUiState,
            deleteDialogUiState = deleteDialogUiState,
            onDismiss = onShowMinimalDialog,
            onShowDeleteDialog = onShowDeleteDialog,
            onShowEditBottomSheet =onShowEditBottomSheet
        )
    }

    if (deleteDialogUiState.isDeleteDialogVisible) {
        DeleteDialog(
            mediaItem = deleteDialogUiState.mediaItem,
            onDismiss = onShowDeleteDialog,
            deleteDialogUiState = deleteDialogUiState,
            minimalDialogUiState = minimalDialogUiState,
            onDismissMinimalDialog = onShowMinimalDialog,
            onDelete = onDelete,
        )
    }
}

@Composable
fun ListItem(
    mediaItem: MediaItem,
    minimalDialogUiState: MinimalDialogUiState,
    onChangeMinimalDialogState: (Boolean, MediaItemDetails) -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            onClick = {
                onNavigateToDetails(mediaItem.id)
            },
            modifier = modifier.padding(2.dp)
        ) {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ListItemIcon(
                    mediaItem = mediaItem
                )
                Spacer(modifier.width(10.dp))
                ListItemInformation(
                    mediaItem.title,
                    mediaItem.createDate.toString(),
                    modifier = modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        onChangeMinimalDialogState(
                            minimalDialogUiState.isMinimalDialogVisible,
                            mediaItem.toMediaItemDetails()
                        )
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = ""
                    )
                }
            }
        }
    }
}

@Composable
fun ListItemInformation(
    name: String,
    date: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = name,
        )
        Text(
            text = date
        )
    }
}

@Composable
fun ListItemIcon(
    mediaItem: MediaItem,
    modifier: Modifier = Modifier
) {
    println("MEDIAITEM SRC: ${mediaItem.src}")
    AsyncImage(
        model = mediaItem.src,
        contentDescription = "",
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(10.dp))
            .width(60.dp)
            .height(60.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomModal(
    context: Context,
    bottomSheetUiState: BottomSheetUiState,
    alertDialogUiState: AlertDialogUiState,
    onShowBottomSheet: (Boolean) -> Unit,
    onSheetItemValueChange: (MediaItemDetails) -> Unit,
    onSave: () -> Unit,
    onImagePicked: (Context, Uri) -> ByteArray?,
    onDelete: (MediaItem) -> Unit,
    onUpdate: (MediaItem) -> Unit,
    mediaItem: MediaItemDetails,
    modifier: Modifier = Modifier
) {
    val transferItem by remember { mutableStateOf(mediaItem) }
    val itemFlag = transferItem.title.isNotEmpty()

    val sheetState = rememberModalBottomSheetState()

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    var imgResult by remember { mutableStateOf<ByteArray?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            imgResult = onImagePicked(context, it)
        }
    }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    ModalBottomSheet(
        onDismissRequest = {
            onShowBottomSheet(bottomSheetUiState.isBottomSheetVisible)
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = modifier
                .padding(8.dp)
                .imePadding()
        ) {
            if (!itemFlag) {
                Text(
                    text="Neues Medium"
                )
            } else {
                Text(
                    text="${transferItem.title} bearbeiten"
                )
            }

            HorizontalDivider(thickness = 2.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = modifier
                        .padding(8.dp)
                        .weight(0.7f)
                        .focusRequester(focusRequester),
                    value = mediaItem.title,
                    onValueChange = {
                        onSheetItemValueChange(mediaItem.copy(title = it))
                    },
                    label = { Text(text="Title") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onSheetItemValueChange(mediaItem.copy(src = imgResult))
                            onSave()
                        }
                    ),
                )
                IconButton(
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                        ))
                    },
                ) {
                   Icon(
                       painter = painterResource(R.drawable.photo_camera_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                       contentDescription = ""
                   )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (!itemFlag) {
                imgResult?.let { bytes ->
                    Log.i("BILD UPDATE1:", "BILD: ${imgResult.hashCode()}")
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                if (imgResult?.isNotEmpty() == true) {
                    imgResult?.let { bytes ->
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        onSheetItemValueChange(mediaItem.copy(src = imgResult))
                    }
                } else {
                    val bitmap = BitmapFactory.decodeByteArray(
                        transferItem.src,
                        0,
                        transferItem.src!!.size
                    )
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    modifier = modifier
                        .padding(8.dp)
                        .weight(1f),
                    enabled = itemFlag,
                    onClick = {
                        onDelete(transferItem.toMediaItem())
                        onShowBottomSheet(bottomSheetUiState.isBottomSheetVisible)
                    }
                ) {
                    Text(
                        text="Löschen"
                    )
                }

                if (!itemFlag) {
                    Button(
                        modifier = modifier
                            .padding(8.dp)
                            .weight(1f),
                        onClick = {
                            onSheetItemValueChange(mediaItem.copy(src = imgResult))
                            onSave()
                        }
                    ) {
                        Text(
                            text="Hinzufügen"
                        )
                    }
                } else {
                    Button(
                        modifier = modifier
                            .padding(8.dp)
                            .weight(1f),
                        onClick = {
                            onUpdate(mediaItem.toMediaItem())
                            onShowBottomSheet(bottomSheetUiState.isBottomSheetVisible)
                        }
                    ) {
                        Text(
                            text="Updaten"
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun EntryCheckAlertDialog(
    alertDialogUiState: AlertDialogUiState,
    bottomSheetUiState: BottomSheetUiState,
    onDismiss: (Boolean) -> Unit,
    onConfirm: (Boolean) -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = ""
            )
        },
        title = {
            Text(text = "Fehlende Eingabe")
        },
        text = {
            Text(text = "Bitte wählen Sie ein Bild aus")
        },
        onDismissRequest = {
            onDismiss(
                alertDialogUiState.isDialogVisible
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss(
                        alertDialogUiState.isDialogVisible
                    )
                    onConfirm(
                        bottomSheetUiState.isBottomSheetVisible
                    )
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss(
                        alertDialogUiState.isDialogVisible
                    )
                }
            ) {
                Text("Dismiss")
            }
        },
    )
}

@Composable
fun MinimalDialog(
    mediaItem: MediaItemDetails,
    minimalDialogUiState: MinimalDialogUiState,
    bottomSheetUiState: BottomSheetUiState,
    deleteDialogUiState: DeleteDialogUiState,
    onDismiss: (Boolean, MediaItemDetails) -> Unit,
    onShowDeleteDialog: (Boolean, MediaItemDetails) -> Unit,
    onShowEditBottomSheet: (MediaItemDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = {
            onDismiss(
                minimalDialogUiState.isMinimalDialogVisible,
                mediaItem
            )
    }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = modifier
                        .weight(1f)
                ) {
                    Text(
                        text = mediaItem.title,
                        modifier = modifier.align(Alignment.Center)
                    )
                }
                HorizontalDivider(thickness = 1.dp)
                Box(
                    modifier = modifier
                        .clickable(
                            onClick = {
                                onShowDeleteDialog(
                                    deleteDialogUiState.isDeleteDialogVisible,
                                    mediaItem
                                )
                            }
                        )
                        .fillMaxWidth()
                        .padding(8.dp)
                        .weight(1f)

                ) {
                    Text(text = "Löschen")
                }
                HorizontalDivider(thickness = 1.dp)
                Box(
                    modifier = modifier
                        .clickable(
                            onClick = {
                                onDismiss(
                                    minimalDialogUiState.isMinimalDialogVisible,
                                    mediaItem
                                )
                                onShowEditBottomSheet(mediaItem)
                            }
                        )
                        .fillMaxWidth()
                        .padding(8.dp)
                        .weight(1f)
                ) {
                    Text(text = "Editieren")
                }

            }
        }
    }
}

@Composable
fun DeleteDialog(
    mediaItem: MediaItemDetails,
    deleteDialogUiState: DeleteDialogUiState,
    minimalDialogUiState: MinimalDialogUiState,
    onDismiss: (Boolean, MediaItemDetails) -> Unit,
    onDismissMinimalDialog: (Boolean, MediaItemDetails) -> Unit,
    onDelete: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {

    val currentItem by rememberUpdatedState(mediaItem.toMediaItem())

    Dialog(
        onDismissRequest = {
            onDismiss(
                deleteDialogUiState.isDeleteDialogVisible,
                mediaItem
            )
            onDismissMinimalDialog(
                minimalDialogUiState.isMinimalDialogVisible,
                mediaItem
            )
        }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = modifier
                        .weight(1f)
                ) {
                    Text(
                        text = mediaItem.title,
                        modifier = modifier.align(Alignment.Center)
                    )
                }
                HorizontalDivider(thickness = 1.dp)
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .weight(1f)

                ) {
                    Text(text = "Möchten Sie ${mediaItem.title} löschen?")
                }
                HorizontalDivider(thickness = 1.dp)
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Button(
                        modifier = modifier
                            .padding(8.dp)
                            .weight(1f),
                        onClick = {
                            onDismiss(
                                deleteDialogUiState.isDeleteDialogVisible,
                                mediaItem
                            )
                        }
                    ) {
                        Text(
                            text="Abbrechen"
                        )
                    }
                    Button(
                        modifier = modifier
                            .padding(8.dp)
                            .weight(1f),
                        onClick = {
                            onDismiss(
                                deleteDialogUiState.isDeleteDialogVisible,
                                mediaItem
                            )
                            onDismissMinimalDialog(
                                minimalDialogUiState.isMinimalDialogVisible,
                                mediaItem
                            )
                            onDelete(currentItem)
                        }
                    ) {
                        Text(
                            text="Löschen"
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(

) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.TopBarTitle)
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
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = ""
                )
            }
        }
    )
}