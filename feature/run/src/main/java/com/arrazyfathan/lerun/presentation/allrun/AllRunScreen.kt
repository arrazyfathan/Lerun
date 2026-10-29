package com.arrazyfathan.lerun.presentation.allrun

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arrazyfathan.lerun.domain.model.Run
import com.arrazyfathan.lerun.domain.model.SortType
import com.arrazyfathan.lerun.presentation.home.MainViewModel
import com.arrazyfathan.lerun.presentation.home.RunAction
import com.arrazyfathan.lerun.ui.designsystem.Inter
import com.arrazyfathan.lerun.ui.designsystem.LerunDarkAccentText
import com.arrazyfathan.lerun.ui.designsystem.LerunGreen
import com.arrazyfathan.lerun.ui.designsystem.LerunMainDark
import com.arrazyfathan.lerun.ui.designsystem.LerunSecondaryDark
import com.arrazyfathan.lerun.ui.designsystem.LerunTheme
import com.arrazyfathan.lerun.utils.TrackingUtility
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.arrazyfathan.lerun.core.ui.R as UiR

@Composable
fun AllRunRoute(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
) {
    AllRunScreen(
        viewModel = viewModel,
        modifier = modifier,
    )
}

@Composable
fun AllRunScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllRunScreenContent(
        runs = state.runs,
        selectedSortType = state.sortType,
        onSortTypeSelected = { sortType ->
            viewModel.onAction(RunAction.OnSortTypeChanged(sortType))
        },
        modifier = modifier,
    )
}

@Composable
fun AllRunScreenContent(
    runs: List<Run>,
    selectedSortType: SortType,
    onSortTypeSelected: (SortType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filterOptions = stringArrayResource(id = UiR.array.filter_options)
    var dropdownExpanded by remember { mutableStateOf(false) }

    val selectedIndex = when (selectedSortType) {
        SortType.DATE -> 0
        SortType.RUNNING_TIME -> 1
        SortType.DISTANCE -> 2
        SortType.AVG_SPEED -> 3
        SortType.CALORIES_BURNED -> 4
        SortType.DATE_ASC -> 5
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "All Activities",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Sort by:",
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box {
                    Row(
                        modifier = Modifier
                            .clickable { dropdownExpanded = true }
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = filterOptions.getOrElse(selectedIndex) { "Date" },
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = UiR.drawable.ic_down),
                            contentDescription = "Sort options",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                    ) {
                        filterOptions.forEachIndexed { index, label ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = label,
                                        fontFamily = Inter,
                                    )
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    val sortType = when (index) {
                                        0 -> SortType.DATE
                                        1 -> SortType.RUNNING_TIME
                                        2 -> SortType.DISTANCE
                                        3 -> SortType.AVG_SPEED
                                        4 -> SortType.CALORIES_BURNED
                                        else -> SortType.DATE_ASC
                                    }
                                    onSortTypeSelected(sortType)
                                },
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = runs,
                key = { run -> run.id ?: run.hashCode() },
            ) { run ->
                ExpandableRunCard(run = run)
            }
        }
    }
}

@Composable
fun ExpandableRunCard(
    run: Run,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val headerBackgroundColor by animateColorAsState(
        targetValue = if (expanded) LerunGreen else LerunSecondaryDark,
        animationSpec = tween(durationMillis = 300),
        label = "headerBg",
    )
    val titleColor by animateColorAsState(
        targetValue = if (expanded) LerunMainDark else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "titleColor",
    )
    val dateColor by animateColorAsState(
        targetValue = if (expanded) LerunMainDark else LerunDarkAccentText,
        animationSpec = tween(durationMillis = 300),
        label = "dateColor",
    )
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "arrowRotation",
    )

    val formattedDate = remember(run.timestamp) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = headerBackgroundColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBackgroundColor)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(60.dp),
                ) {
                    if (run.img != null) {
                        Image(
                            bitmap = run.img!!.asImageBitmap(),
                            contentDescription = run.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LerunMainDark),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = run.title,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formattedDate,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = dateColor,
                    )
                }

                Icon(
                    painter = painterResource(id = UiR.drawable.ic_down),
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = titleColor,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .rotate(arrowRotation),
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)),
            ) {
                ExpandableRunStatsGrid(
                    distance = "${run.distanceInMeters / 1000f} Km",
                    avgSpeed = "${run.avgSpeedInKMH}km/h",
                    duration = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis),
                    calories = "${run.caloriesBurned}kcal",
                )
            }
        }
    }
}

@Composable
private fun ExpandableRunStatsGrid(
    distance: String,
    avgSpeed: String,
    duration: String,
    calories: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(LerunGreen),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                ExpandableStatCell(
                    label = "Distance",
                    value = distance,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                VerticalDivider(
                    thickness = 1.dp,
                    color = LerunMainDark.copy(alpha = 0.25f),
                )
                ExpandableStatCell(
                    label = "Average Speed",
                    value = avgSpeed,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = LerunMainDark.copy(alpha = 0.25f),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                ExpandableStatCell(
                    label = "Duration",
                    value = duration,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                VerticalDivider(
                    thickness = 1.dp,
                    color = LerunMainDark.copy(alpha = 0.25f),
                )
                ExpandableStatCell(
                    label = "Calories Burned",
                    value = calories,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun ExpandableStatCell(
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
            fontSize = 14.sp,
            color = LerunMainDark,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = value,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = LerunMainDark,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AllRunScreenPreview() {
    LerunTheme(darkTheme = true) {
        AllRunScreenContent(
            runs = listOf(
                Run(
                    id = 1,
                    title = "Morning Run",
                    timestamp = System.currentTimeMillis(),
                    avgSpeedInKMH = 10.5f,
                    distanceInMeters = 5200,
                    timeInMillis = 1800000L,
                    caloriesBurned = 450,
                ),
            ),
            selectedSortType = SortType.DATE,
            onSortTypeSelected = {},
        )
    }
}
