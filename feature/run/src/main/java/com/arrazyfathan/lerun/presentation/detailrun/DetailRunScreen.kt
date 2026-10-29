package com.arrazyfathan.lerun.presentation.detailrun

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.presentation.home.MainViewModel
import com.arrazyfathan.lerun.presentation.home.RunAction
import com.arrazyfathan.lerun.ui.designsystem.Inter
import com.arrazyfathan.lerun.ui.designsystem.LerunTextDark
import com.arrazyfathan.lerun.ui.designsystem.LerunTheme
import com.arrazyfathan.lerun.utils.TrackingUtility
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.arrazyfathan.lerun.core.ui.R as UiR

@Composable
fun DetailRunRoute(
    runId: Int?,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(runId) {
        if (runId != null) {
            viewModel.onAction(RunAction.OnLoadRunById(runId))
        }
    }

    state.selectedRun?.let { run ->
        DetailRunScreen(
            run = run,
            modifier = modifier,
        )
    }
}

@Composable
fun DetailRunScreen(
    run: Run,
    modifier: Modifier = Modifier,
) {
    val formattedDate = remember(run.timestamp) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }

    val distanceInKm = "${run.distanceInMeters / 1000f} Km"
    val durationFormatted = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
    val caloriesFormatted = "${run.caloriesBurned}kcal"
    val avgSpeedFormatted = "${run.avgSpeedInKMH}km/h"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Text(
            text = stringResource(id = UiR.string.detail_run_label),
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = run.title,
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp),
            )

            Text(
                text = formattedDate,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = LerunTextDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 5.dp),
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .padding(horizontal = 24.dp)
                    .padding(top = 10.dp),
            ) {
                if (run.img != null) {
                    Image(
                        bitmap = run.img!!.asImageBitmap(),
                        contentDescription = run.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Image(
                        painter = painterResource(id = UiR.drawable.setup_bg),
                        contentDescription = run.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 24.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        DetailStatCell(
                            label = "Distance",
                            value = distanceInKm,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                        VerticalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        )
                        DetailStatCell(
                            label = "Average Speed",
                            value = avgSpeedFormatted,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        DetailStatCell(
                            label = "Duration",
                            value = durationFormatted,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                        VerticalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        )
                        DetailStatCell(
                            label = "Calories Burned",
                            value = caloriesFormatted,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailStatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = LerunTextDark,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = value,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailRunScreenPreview() {
    LerunTheme(darkTheme = true) {
        DetailRunScreen(
            run = Run(
                id = 1,
                title = "Morning Run",
                timestamp = System.currentTimeMillis(),
                avgSpeedInKMH = 9f,
                distanceInMeters = 10000,
                timeInMillis = 1820000L,
                caloriesBurned = 620,
            ),
        )
    }
}
