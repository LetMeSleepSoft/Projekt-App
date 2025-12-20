package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.dieschnittstelle.mobile.android.kotlin.skeleton.R
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.theme.MADDemoTheme


@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    viewModel: MediaItemViewModel = hiltViewModel()
){
    val coroutineScope = rememberCoroutineScope()
    val mediaItemListUiState by viewModel.mediaItemListUiState.collectAsState()
    val context: Context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(

            )
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
            mediaItemListUiState = mediaItemListUiState,
            onShowBottomSheet = viewModel::changeBottomSheetUiState,
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
            onImagePicked = viewModel::uriToByteArray,
            context = context,
        )
    }
}

@Composable
fun ListScreenBody(
    bottomSheetUiState: BottomSheetUiState,
    mediaItemListUiState: MediaItemListUiState,
    onShowBottomSheet: (Boolean) -> Unit,
    onSheetItemValueChange: (MediaItemDetails) -> Unit,
    mediaItems: List<MediaItem>,
    onSave: () -> Unit,
    onImagePicked: (Context, Uri) -> ByteArray?,
    context: Context,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(items = mediaItems) { item ->
            ListItem(
                mediaItem = item
            )
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
        )
    }

}

@Composable
fun ListItem(
    mediaItem: MediaItem,
    modifier: Modifier = Modifier
) {
    Box {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
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
                    onClick = {},
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomModal(
    context: Context,
    bottomSheetUiState: BottomSheetUiState,
    onShowBottomSheet: (Boolean) -> Unit,
    onSheetItemValueChange: (MediaItemDetails) -> Unit,
    onSave: () -> Unit,
    onImagePicked: (Context, Uri) -> ByteArray?,
    mediaItem: MediaItemDetails,
    modifier: Modifier = Modifier
) {
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
            Text(
                text="Neues Medium"
            )
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
                        onDone = { onSave() }
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
            Spacer(modifier = modifier.height(8.dp))
            imgResult?.let { bytes ->
                Spacer(modifier = Modifier.height(16.dp))

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

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    modifier = modifier
                        .padding(8.dp)
                        .weight(1f),
                    onClick = {}
                ) {
                    Text(
                        text="Löschen"
                    )
                }
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
            }
        }
    }
}



@Preview
@Composable
fun ListScreenPreview() {
    MADDemoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ListScreen(
                Modifier.padding(innerPadding)
            )
        }
    }
}