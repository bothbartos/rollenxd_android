package com.bartosboth.rollen_android.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bartosboth.rollen_android.data.model.user.UserDetail
import com.bartosboth.rollen_android.ui.components.AppTopBar
import com.bartosboth.rollen_android.ui.components.CircularBase64ImageButton
import com.bartosboth.rollen_android.ui.components.CustomButton
import com.bartosboth.rollen_android.ui.components.CustomTextField
import com.bartosboth.rollen_android.ui.components.SongListItem
import com.bartosboth.rollen_android.ui.screens.main.LogoutViewModel
import com.bartosboth.rollen_android.ui.screens.main.UpdateState
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
fun ProfileScreen(
    userDetail: UserDetail,
    logoutViewModel: LogoutViewModel,
    updateState: UpdateState,
    onProfileUpdate: (String, Uri?) -> Unit,
    navController: NavController
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditForm by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    logoutViewModel.logout()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "RollenXd"
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    CircularBase64ImageButton(
                        userDetail = userDetail,
                        size = 130.dp,
                        modifier = Modifier.padding(5.dp),
                        onClick = {showEditForm = true}
                    )
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(userDetail.name)
                        Text(userDetail.email)
                    }
                }
                if(userDetail.songs.isEmpty()){
                    Text("No songs uploaded")
                }else{
                    Text("Your Songs:")
                    LazyRow {
                        itemsIndexed(userDetail.songs) { index, song ->
                            SongListItem(
                                song = song,
                                isPlaying = false,
                                onClick = {  }
                            )
                        }
                    }
                }
                if(showEditForm){
                    UserEditForm(
                        userDetail = userDetail,
                        updateState = updateState,
                        onSubmit =  onProfileUpdate,
                        onDismiss = { showEditForm = false }
                        )
                }
                CustomButton(
                    "Logout",
                    onClick = { showLogoutDialog = true },
                    isEnabled = true,
                    isLoading = false,
                )
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun UserEditForm(
    userDetail: UserDetail,
    updateState: UpdateState,
    onSubmit: (String, Uri?) -> Unit,
    onDismiss: () -> Unit
) {
    var bio by remember { mutableStateOf(userDetail.bio) }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        profilePictureUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Edit Profile",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CustomTextField(
            value = bio,
            onValueChange = { bio = it },
            label = "Bio"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomButton(
            text = "Select Profile Picture",
            onClick = { pickMedia.launch(
                PickVisualMediaRequest(
                    mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            ) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        profilePictureUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Selected profile picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CustomButton(
                text = "Cancel",
                onClick = onDismiss,
                isEnabled = updateState !is UpdateState.Loading
            )

            CustomButton(
                text = "Save",
                onClick = {
                    profilePictureUri?.let { uri ->

                            onSubmit(bio, uri)
                    } ?: onSubmit(bio, null)
                },
                isEnabled = updateState !is UpdateState.Loading,
                isLoading = updateState is UpdateState.Loading
            )
        }
    }
}


@Preview
@Composable
fun EditFormPreview() {
    val userDetail = UserDetail(
        id = 1L,
        name = "",
        email = "",
        bio = "",
        profileImageBase64 = "",
        songs = emptyList(),
    )
    UserEditForm(
        userDetail = userDetail,
        updateState = UpdateState.Idle,
        onSubmit = { _, _ -> },
        onDismiss = {}
    )
}