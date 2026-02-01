package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dieschnittstelle.mobile.android.kotlin.skeleton.R
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toMediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.toMediaItemDetails
import java.util.UUID

enum class ListFilter {
    ALL,
    LOCAL,
    REMOTE
}

@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToMap: () -> Unit,
    viewModel: MediaItemViewModel = hiltViewModel()
){
    val coroutineScope = rememberCoroutineScope()
    val mediaItemListUiState by viewModel.localMediaItemListUiState.collectAsState()
    val remoteItemListUiState by viewModel.remoteItemListUiState.collectAsState()
    val allMediaItemsUiState by viewModel.allMediaItemsUiState.collectAsState()
    val context: Context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var listFilter by remember { mutableStateOf(ListFilter.LOCAL) }

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
                            modifier = Modifier.padding(16.dp),
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
                    onClick = {  }
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
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    scope = coroutineScope,
                    drawerstate = drawerState,
                )
            },
            bottomBar = {
                BottomAppBar(
                    actions = {
                        Button(
                            onClick = { listFilter = ListFilter.LOCAL  },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (listFilter.name == "LOCAL") {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp)
                        ) {
                            Text(
                                text = "Lokal"
                            )
                        }
                        Button(
                            onClick = { listFilter = ListFilter.REMOTE },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (listFilter.name == "REMOTE") {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp)
                        ) {
                            Text(
                                text = "Remote"
                            )
                        }
                        Button(
                            onClick = { listFilter = ListFilter.ALL },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (listFilter.name == "ALL") {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    start = 2.dp,
                                    top = 2.dp,
                                    bottom = 2.dp,
                                    end = 16.dp
                                )
                        ) {
                            Text(
                                text = "Alle"
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                viewModel.changeBottomSheetUiState(
                                    viewModel.bottomSheetUiState.isBottomSheetVisible
                                )
                            },
                        ) {
                            Icon(Icons.Filled.Add, "Localized description")
                        }
                    }
                )
            },
        ) { innerPadding ->
            ListScreenBody(
                bottomSheetUiState = viewModel.bottomSheetUiState,
                alertDialogState = viewModel.alertDialogUiState,
                onShowBottomSheet = viewModel::changeBottomSheetUiState,
                onShowEditBottomSheet = viewModel::transferMediaItemToBottomUiState,
                onSheetItemValueChange = viewModel::updateBottomSheetUiState,
                mediaItems = mediaItemListUiState.mediaItems,
                remoteItems = remoteItemListUiState.mediaItems,
                allItems = allMediaItemsUiState.mediaItems,
                modifier = Modifier.padding(innerPadding),
                onSave = {
                    coroutineScope.launch {
                        viewModel.saveMediaItem(it)
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
                onNavigateToDetails = onNavigateToDetails,
                onChosenButton = viewModel::changeLocalRemoteButton,
                listFilter = listFilter
            )
        }
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
    remoteItems: List<MediaItem>,
    allItems: List<MediaItem>,
    onSave: (String) -> Unit,
    onDelete: (MediaItem) -> Unit,
    onUpdate: (MediaItem) -> Unit,
    onImagePicked: (Context, Uri) -> ByteArray?,
    onDismiss: (Boolean) -> Unit,
    onShowMinimalDialog: (Boolean, MediaItemDetails) -> Unit,
    onShowDeleteDialog: (Boolean, MediaItemDetails) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onChosenButton:(Boolean) -> Unit,
    context: Context,
    listFilter: ListFilter,
    modifier: Modifier = Modifier
) {

    when (listFilter) {
        ListFilter.REMOTE -> {
            LazyColumn(
                modifier = modifier
            ) {
                items(
                    items = remoteItems,
                    key = { it.remoteKey }
                ) { item ->
                    ListItem(
                        mediaItem = item,
                        minimalDialogUiState = minimalDialogUiState,
                        onChangeMinimalDialogState = onShowMinimalDialog,
                        onNavigateToDetails = onNavigateToDetails
                    )
                }
            }
        }
        ListFilter.ALL -> {
            LazyColumn(
                modifier = modifier
            ) {
                items(
                    items = allItems,
                    key = { item -> item.id }
                ) { item ->
                    ListItem(
                        mediaItem = item,
                        minimalDialogUiState = minimalDialogUiState,
                        onChangeMinimalDialogState = onShowMinimalDialog,
                        onNavigateToDetails = onNavigateToDetails
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = modifier
            ) {
                items(
                    items = mediaItems,
                ) { item ->
                    ListItem(
                        mediaItem = item,
                        minimalDialogUiState = minimalDialogUiState,
                        onChangeMinimalDialogState = onShowMinimalDialog,
                        onNavigateToDetails = onNavigateToDetails
                    )
                }
            }
        }
    }


    if (bottomSheetUiState.isBottomSheetVisible) {
        BottomModal(
            bottomSheetUiState = bottomSheetUiState,
            onShowBottomSheet = onShowBottomSheet,
            onSheetItemValueChange = onSheetItemValueChange,
            mediaItem = bottomSheetUiState.mediaItemDetails,
            onSave = onSave,
            context = context,
            onImagePicked = onImagePicked,
            onDelete = onDelete,
            onUpdate = onUpdate,
            onChoseButton = onChosenButton,
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
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val stringFromUUID = mediaItem.remoteId.toString()

    Box {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            onClick = {
                if (stringFromUUID == "00000000-0000-0000-0000-000000000000") {
                    onNavigateToDetails(
                        mediaItem.id.toString(),
                    )
                } else {
                    onNavigateToDetails(
                        mediaItem.remoteId.toString(),
                    )
                }

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
                    mediaItem.remoteId,
                    modifier = modifier.weight(1f)
                )
                if (stringFromUUID == "00000000-0000-0000-0000-000000000000") {
                    Text(text = "Local")
                } else {
                    Text(text = "Remote")
                }
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
    remoteId: UUID,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = name,
            fontSize = 20.sp
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
    onShowBottomSheet: (Boolean) -> Unit,
    onSheetItemValueChange: (MediaItemDetails) -> Unit,
    onSave: (String) -> Unit,
    onImagePicked: (Context, Uri) -> ByteArray?,
    onDelete: (MediaItem) -> Unit,
    onUpdate: (MediaItem) -> Unit,
    onChoseButton: (Boolean) -> Unit,
    mediaItem: MediaItemDetails,
    modifier: Modifier = Modifier
) {
    val transferItem by remember { mutableStateOf(mediaItem) }
    val itemFlag = transferItem.title.isNotEmpty()

    val buttonFlag by remember { mutableStateOf(true) }

    val sheetState = rememberModalBottomSheetState()

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    var imgResult by remember { mutableStateOf<ByteArray?>(null) }
    var imageName by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            imgResult = onImagePicked(context, it)
            val cursor = context.contentResolver.query(
                uri,
                null,
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    imageName = it.getString(nameIndex)
                }
            }
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
                            onSave(imageName)
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

            if (!itemFlag) {
               Box(
                   //modifier = modifier.background(color = Color.Gray)
               ){
                   Row() {
                       Button(
                           onClick = {
                               onChoseButton(buttonFlag)
                           },
                           shape = RoundedCornerShape(4.dp),
                           colors = if (bottomSheetUiState.isLocalButtonChosen)
                               ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary) else
                               ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface),
                           modifier = modifier
                               .padding(2.dp)
                               .weight(1f)
                       ) {
                           Text(
                               text = "Lokal"
                           )
                       }
                       Button(
                           onClick = {
                               onChoseButton(!buttonFlag)
                           },
                           shape = RoundedCornerShape(4.dp),
                           colors = if (bottomSheetUiState.isLocalButtonChosen)
                               ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface) else
                               ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                           modifier = modifier
                               .padding(2.dp)
                               .weight(1f)
                       ) {
                           Text(
                               text = "Remote"
                           )
                       }
                   }
               }
            }

            HorizontalDivider(thickness = 2.dp)

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
                            onSave(imageName)
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
    scope: CoroutineScope,
    drawerstate: DrawerState
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.TopBarTitle)
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    drawerstate.open()
                }
            }) {
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