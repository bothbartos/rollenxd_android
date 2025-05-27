package com.bartosboth.rollen_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bartosboth.rollen_android.ui.navigation.RollenXdNavigation
import com.bartosboth.rollen_android.ui.theme.Rollen_androidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Rollen_androidTheme {
                        RollenXdNavigation()
            }
        }
    }
}
