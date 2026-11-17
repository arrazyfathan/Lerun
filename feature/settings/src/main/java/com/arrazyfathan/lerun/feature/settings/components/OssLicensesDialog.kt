package com.arrazyfathan.lerun.feature.settings.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arrazyfathan.lerun.ui.designsystem.Inter
import com.arrazyfathan.lerun.ui.designsystem.LerunGreen
import com.arrazyfathan.lerun.ui.designsystem.LerunMainDark
import com.arrazyfathan.lerun.ui.designsystem.LerunTextDark
import com.arrazyfathan.lerun.core.ui.R as UiR

@Immutable
data class OpenSourceLibrary(
    val name: String,
    val artifact: String,
    val author: String,
    val licenseName: String,
    val url: String,
    val licenseText: String = APACHE_2_0_SUMMARY,
)

@Composable
fun OssLicensesDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val libraries = remember { defaultOpenSourceLibraries }
    var expandedArtifact by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Open Source Licenses",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Libraries and attributions used in Lerun",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = LerunTextDark,
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.clip(CircleShape),
                ) {
                    Icon(
                        painter = painterResource(id = UiR.drawable.ic_close_white),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = libraries,
                    key = { it.artifact },
                ) { library ->
                    val isExpanded = expandedArtifact == library.artifact
                    LicenseLibraryCard(
                        library = library,
                        expanded = isExpanded,
                        onToggleExpand = {
                            expandedArtifact = if (isExpanded) null else library.artifact
                        },
                        onOpenWebsite = {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(library.url)),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LicenseLibraryCard(
    library: OpenSourceLibrary,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenWebsite: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = library.name,
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${library.artifact} • ${library.author}",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = LerunTextDark,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = LerunGreen.copy(alpha = 0.16f),
                ) {
                    Text(
                        text = library.licenseName,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = LerunGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(220)),
                exit = shrinkVertically(animationSpec = tween(180)),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(12.dp),
                    ) {
                        Text(
                            text = library.licenseText,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onOpenWebsite) {
                            Text(
                                text = "View Repository",
                                fontFamily = Inter,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = LerunGreen,
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val APACHE_2_0_SUMMARY =
    "Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this " +
        "file except in compliance with the License. You may obtain a copy of the License at " +
        "https://www.apache.org/licenses/LICENSE-2.0\n\n" +
        "Unless required by applicable law or agreed to in writing, software distributed under " +
        "the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF " +
        "ANY KIND, either express or implied."

private val defaultOpenSourceLibraries =
    listOf(
        OpenSourceLibrary(
            name = "Jetpack Compose (UI, Foundation, Material 3)",
            artifact = "androidx.compose",
            author = "Google / Android Open Source Project",
            licenseName = "Apache 2.0",
            url = "https://developer.android.com/jetpack/compose",
        ),
        OpenSourceLibrary(
            name = "Jetpack Navigation 3",
            artifact = "androidx.navigation3:navigation3-runtime",
            author = "Google / Android Open Source Project",
            licenseName = "Apache 2.0",
            url = "https://developer.android.com/guide/navigation",
        ),
        OpenSourceLibrary(
            name = "AndroidX Lifecycle & ViewModel",
            artifact = "androidx.lifecycle",
            author = "Google / Android Open Source Project",
            licenseName = "Apache 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/lifecycle",
        ),
        OpenSourceLibrary(
            name = "AndroidX Room Persistence Library",
            artifact = "androidx.room:room-ktx",
            author = "Google / Android Open Source Project",
            licenseName = "Apache 2.0",
            url = "https://developer.android.com/training/data-storage/room",
        ),
        OpenSourceLibrary(
            name = "Dagger Hilt",
            artifact = "com.google.dagger:hilt-android",
            author = "Google",
            licenseName = "Apache 2.0",
            url = "https://dagger.dev/hilt/",
        ),
        OpenSourceLibrary(
            name = "Kotlin Standard Library & Coroutines",
            artifact = "org.jetbrains.kotlinx:kotlinx-coroutines-android",
            author = "JetBrains",
            licenseName = "Apache 2.0",
            url = "https://github.com/Kotlin/kotlinx.coroutines",
        ),
        OpenSourceLibrary(
            name = "Kotlinx Serialization",
            artifact = "org.jetbrains.kotlinx:kotlinx-serialization-json",
            author = "JetBrains",
            licenseName = "Apache 2.0",
            url = "https://github.com/Kotlin/kotlinx.serialization",
        ),
        OpenSourceLibrary(
            name = "Haze",
            artifact = "dev.chrisbanes.haze:haze",
            author = "Chris Banes",
            licenseName = "Apache 2.0",
            url = "https://github.com/chrisbanes/haze",
        ),
        OpenSourceLibrary(
            name = "MPAndroidChart",
            artifact = "com.github.PhilJay:MPAndroidChart",
            author = "Philipp Jahoda",
            licenseName = "Apache 2.0",
            url = "https://github.com/PhilJay/MPAndroidChart",
        ),
        OpenSourceLibrary(
            name = "Google Play Services Maps & Location",
            artifact = "com.google.android.gms:play-services-maps",
            author = "Google",
            licenseName = "Android SDK Terms",
            url = "https://developers.google.com/maps/documentation/android-sdk",
        ),
        OpenSourceLibrary(
            name = "Firebase Android SDK",
            artifact = "com.google.firebase:firebase-bom",
            author = "Google",
            licenseName = "Apache 2.0",
            url = "https://github.com/firebase/firebase-android-sdk",
        ),
        OpenSourceLibrary(
            name = "Timber",
            artifact = "com.jakewharton.timber:timber",
            author = "Jake Wharton",
            licenseName = "Apache 2.0",
            url = "https://github.com/JakeWharton/timber",
        ),
    )
