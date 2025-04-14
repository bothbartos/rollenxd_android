package com.bartosboth.rollen_android.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bartosboth.rollen_android.data.model.UserDetail
import com.bartosboth.rollen_android.ui.components.AppTopBar
import com.bartosboth.rollen_android.ui.components.CircularBase64ImageButton
import com.bartosboth.rollen_android.ui.components.CustomButton
import com.bartosboth.rollen_android.ui.components.SongListItem
import com.bartosboth.rollen_android.ui.screens.main.LogoutViewModel

@Composable
fun ProfileScreen(
    userDetail: UserDetail,
    logoutViewModel: LogoutViewModel,
    navController: NavController
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                    CircularBase64ImageButton(userDetail = userDetail, size = 130.dp, modifier = Modifier.padding(5.dp))
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

