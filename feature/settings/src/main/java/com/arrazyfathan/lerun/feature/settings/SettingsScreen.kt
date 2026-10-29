package com.arrazyfathan.lerun.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arrazyfathan.lerun.ui.designsystem.Inter
import com.arrazyfathan.lerun.ui.designsystem.LerunBlueDark
import com.arrazyfathan.lerun.ui.designsystem.LerunErrorColor
import com.arrazyfathan.lerun.ui.designsystem.LerunGreen
import com.arrazyfathan.lerun.ui.designsystem.LerunMainDark
import com.arrazyfathan.lerun.ui.designsystem.LerunTextDark
import com.arrazyfathan.lerun.ui.designsystem.LerunTheme
import com.arrazyfathan.lerun.utils.BitmapConverter
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import timber.log.Timber
import com.arrazyfathan.lerun.core.ui.R as UiR

@Composable
fun SettingsRoute(
    viewModel: SettingViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                is SettingEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                SettingEvent.OpenLicenses -> {
                    context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                }
            }
        }
    }

    SettingsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
    )
}

@Composable
fun SettingsScreen(
    state: SettingState,
    snackbarHostState: SnackbarHostState,
    onAction: (SettingAction) -> Unit,
) {
    val context = LocalContext.current

    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uriContent ->
                val converted = BitmapConverter.convertImageUriToBase64(uriContent, context)
                onAction(SettingAction.ChangeImage(converted))
            }
        } else {
            Timber.e(result.error)
        }
    }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            val options = CropImageOptions().apply {
                guidelines = CropImageView.Guidelines.ON
            }
            cropImageLauncher.launch(
                CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = options,
                ),
            )
        } else {
            Timber.tag("PhotoPicker").d("No media selected")
        }
    }

    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        }.getOrDefault("1.0")
    }

    if (state.showEditProfileSheet) {
        EditProfileBottomSheet(
            initialName = state.username,
            initialWeight = state.weight.toString(),
            profileImageBase64 = state.profileImageBase64,
            onPickImageClick = {
                pickMediaLauncher.launch(
                    PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        .build(),
                )
            },
            onSaveProfile = { newName, newWeight ->
                onAction(SettingAction.SaveProfile(newName, newWeight))
            },
            onDismiss = { onAction(SettingAction.DismissEditProfileSheet) },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        SettingsScreenContent(
            username = state.username,
            profileImageBase64 = state.profileImageBase64,
            isDarkTheme = state.isDarkTheme,
            versionText = stringResource(id = UiR.string.version_name, versionName),
            onEditProfileClick = { onAction(SettingAction.ShowEditProfileSheet) },
            onDarkThemeToggle = { checked ->
                onAction(SettingAction.SetDarkTheme(checked))
            },
            onLicensesClick = {
                onAction(SettingAction.OpenLicenses)
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun SettingsScreenContent(
    username: String,
    profileImageBase64: String?,
    isDarkTheme: Boolean,
    versionText: String,
    onEditProfileClick: () -> Unit,
    onDarkThemeToggle: (Boolean) -> Unit,
    onLicensesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val decodedBitmap = remember(profileImageBase64) {
        if (!profileImageBase64.isNullOrBlank()) {
            BitmapConverter.converterStringToBitmap(profileImageBase64)
        } else {
            null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
    ) {
        Text(
            text = stringResource(id = UiR.string.settings_label),
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(top = 24.dp)
                .size(100.dp),
        ) {
            if (decodedBitmap != null) {
                Image(
                    bitmap = decodedBitmap.asImageBitmap(),
                    contentDescription = stringResource(id = UiR.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Image(
                    painter = painterResource(id = UiR.drawable.profile_n),
                    contentDescription = stringResource(id = UiR.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Text(
            text = username.ifBlank { "Username" },
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp),
        )

        SettingsItemCard(
            title = "Edit Profile",
            subtitle = "Change name and weight",
            onClick = onEditProfileClick,
            modifier = Modifier.padding(top = 16.dp),
        )

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(70.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = stringResource(id = UiR.string.themes_label),
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Use dark theme",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = LerunTextDark,
                    )
                }

                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onDarkThemeToggle,
                )
            }
        }

        SettingsItemCard(
            title = "Licenses",
            subtitle = "Open source licenses",
            onClick = onLicensesClick,
            modifier = Modifier.padding(top = 8.dp),
        )

        SettingsItemCard(
            title = "Version",
            subtitle = versionText,
            onClick = null,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun SettingsItemCard(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .let {
                if (onClick != null) it.clickable(onClick = onClick) else it
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                fontFamily = Inter,
                fontWeight = FontWeight.Light,
                fontSize = 12.sp,
                color = LerunTextDark,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileBottomSheet(
    initialName: String,
    initialWeight: String,
    profileImageBase64: String?,
    onPickImageClick: () -> Unit,
    onSaveProfile: (name: String, weight: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf(initialName) }
    var weight by rememberSaveable { mutableStateOf(initialWeight) }
    var showValidation by rememberSaveable { mutableStateOf(false) }

    val decodedBitmap = remember(profileImageBase64) {
        if (!profileImageBase64.isNullOrBlank()) {
            BitmapConverter.converterStringToBitmap(profileImageBase64)
        } else {
            null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Profile",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.clip(CircleShape),
                ) {
                    Icon(
                        painter = painterResource(id = UiR.drawable.ic_close_white),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp)

            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(100.dp),
            ) {
                if (decodedBitmap != null) {
                    Image(
                        bitmap = decodedBitmap.asImageBitmap(),
                        contentDescription = stringResource(id = UiR.string.app_name),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Image(
                        painter = painterResource(id = UiR.drawable.profile_n),
                        contentDescription = stringResource(id = UiR.string.app_name),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            TextButton(
                onClick = onPickImageClick,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    text = "Change Image Profile",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = LerunBlueDark,
                )
            }

            if (showValidation) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LerunErrorColor)
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Title cannot be empty",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            ProfileSheetInputField(
                value = name,
                onValueChange = {
                    name = it
                    showValidation = false
                },
                hint = "Enter your name",
                keyboardOptions = KeyboardOptions.Default,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 10.dp),
            )

            ProfileSheetInputField(
                value = weight,
                onValueChange = {
                    weight = it
                    showValidation = false
                },
                hint = "Enter your weight",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 10.dp),
            )

            Button(
                onClick = {
                    if (name.isBlank() || weight.isBlank()) {
                        showValidation = true
                    } else {
                        onSaveProfile(name.trim(), weight.trim())
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LerunGreen,
                    contentColor = LerunMainDark,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp)
                    .height(50.dp),
            ) {
                Text(
                    text = "Save",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = LerunMainDark,
                )
            }
        }
    }
}

@Composable
private fun ProfileSheetInputField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        textStyle = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
        ),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = hint,
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        color = LerunTextDark,
                    )
                }
                innerTextField()
            }
        },
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    LerunTheme(darkTheme = true) {
        SettingsScreenContent(
            username = "Ar Razy Fathan",
            profileImageBase64 = null,
            isDarkTheme = true,
            versionText = "Lerun version v1.2.1",
            onEditProfileClick = {},
            onDarkThemeToggle = {},
            onLicensesClick = {},
        )
    }
}
