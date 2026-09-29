package com.stencilla.app.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stencilla.app.data.local.db.PlannerEventEntity
import com.stencilla.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddEventSheet by remember { mutableStateOf(false) }
    var showTripSheet by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<PlannerEventEntity?>(null) }

    Scaffold(
        containerColor = StencillaOffWhite,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Planner", style = MaterialTheme.typography.headlineMedium, color = StencillaDarkGray)
                    Text("Schedule & outfit planning", style = MaterialTheme.typography.bodySmall, color = StencillaGray)
                }
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { showAddEventSheet = true },
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(StencillaTerracotta),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add event", tint = StencillaCardWhite)
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // ── Calendar Widget ──────────────────────────────────────────────
            item { CalendarWidget(uiState = uiState, viewModel = viewModel) }

            // ── Upcoming Events ──────────────────────────────────────────────
            item {
                SectionHeader(
                    title = "Upcoming Events",
                    actionLabel = "See all",
                    onAction = { /* navigate to full list */ },
                )
            }
            if (uiState.upcomingEvents.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Outlined.EventNote,
                        message = "No upcoming events",
                        subtitle = "Tap + to add a meeting, date, or special occasion.",
                    )
                }
            } else {
                items(uiState.upcomingEvents.take(5), key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onClick = { selectedEvent = event },
                        onPlanOutfit = { viewModel.planOutfitForEvent(event.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }
            }

            // ── Plan Your Day ────────────────────────────────────────────────
            item { PlanYourDayCard(onAdd = { showAddEventSheet = true }) }

            // ── Outfit Calendar Strip ────────────────────────────────────────
            item { OutfitCalendarStrip(uiState = uiState, viewModel = viewModel) }

            // ── Trip Planner Banner ──────────────────────────────────────────
            item { TripPlannerBanner(onClick = { showTripSheet = true }) }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddEventSheet) {
        EventEditorSheet(
            initial = null,
            isTrip = false,
            onSave = { title, date, time, occasion, dressCode, notes ->
                viewModel.createEvent(title, date, time, occasion, dressCode, notes, false, null)
                showAddEventSheet = false
            },
            onDismiss = { showAddEventSheet = false },
        )
    }

    if (showTripSheet) {
        EventEditorSheet(
            initial = null,
            isTrip = true,
            onSave = { title, date, time, occasion, dressCode, notes ->
                viewModel.createEvent(title, date, time, occasion, dressCode, notes, true, null)
                showTripSheet = false
            },
            onDismiss = { showTripSheet = false },
        )
    }

    selectedEvent?.let { event ->
        EventDetailSheet(
            event = event,
            onDismiss = { selectedEvent = null },
            onDelete = { viewModel.deleteEvent(event); selectedEvent = null },
            onPlanOutfit = { viewModel.planOutfitForEvent(event.id); selectedEvent = null },
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Calendar Widget
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun CalendarWidget(uiState: PlannerUiState, viewModel: PlannerViewModel) {
    val today = LocalDate.now()
    val selectedMonth = uiState.selectedMonth
    val eventDates = uiState.calendarEvents.map { it.eventDate }.toSet()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Outlined.ChevronLeft, null, tint = StencillaDarkGray)
                }
                Text(
                    text = selectedMonth.month.getDisplayName(JTextStyle.FULL, Locale.getDefault()) +
                            " ${selectedMonth.year}",
                    style = MaterialTheme.typography.titleSmall,
                    color = StencillaDarkGray,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Outlined.ChevronRight, null, tint = StencillaDarkGray)
                }
            }

            // Day-of-week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                    Text(
                        day, modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = StencillaGray,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            // Days grid
            val firstDay = selectedMonth.atDay(1).dayOfWeek.value % 7 // Sunday=0
            val daysInMonth = selectedMonth.lengthOfMonth()
            val totalCells = firstDay + daysInMonth
            val weeks = (totalCells + 6) / 7

            for (week in 0 until weeks) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val dayNum = week * 7 + col - firstDay + 1
                        if (dayNum < 1 || dayNum > daysInMonth) {
                            Spacer(Modifier.weight(1f))
                        } else {
                            val date = selectedMonth.atDay(dayNum)
                            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val isToday = date == today
                            val hasEvent = dateStr in eventDates
                            val isSelected = uiState.selectedDate == dateStr

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> StencillaTerracotta
                                            isToday -> StencillaTerracottaLight
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { viewModel.selectDate(dateStr) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        dayNum.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = when {
                                            isSelected -> StencillaCardWhite
                                            isToday -> StencillaTerracotta
                                            else -> StencillaDarkGray
                                        },
                                    )
                                    if (hasEvent) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) StencillaCardWhite else StencillaTerracotta),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Plan Your Day card
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun PlanYourDayCard(onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaLavender),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Plan Your Day",
                    style = MaterialTheme.typography.titleSmall,
                    color = StencillaLavenderDark,
                )
                Text(
                    "Add events so Stencilla can suggest the perfect outfit for each one.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StencillaLavenderDark.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = StencillaLavenderDark),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text("Add Event", style = MaterialTheme.typography.labelMedium, color = StencillaCardWhite)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Outfit Calendar Strip
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun OutfitCalendarStrip(uiState: PlannerUiState, viewModel: PlannerViewModel) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        SectionHeader(title = "Outfit Calendar", actionLabel = null, onAction = {})
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val today = LocalDate.now()
            items(14) { offset ->
                val date = today.plusDays(offset.toLong())
                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val event = uiState.calendarEvents.firstOrNull { it.eventDate == dateStr }
                val hasOutfit = !event?.plannedOutfitIds.isNullOrBlank()

                Card(
                    modifier = Modifier.width(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (offset == 0) StencillaTerracottaLight else StencillaCardWhite,
                    ),
                    border = if (offset == 0) ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(StencillaTerracotta)
                    ) else null,
                    elevation = CardDefaults.cardElevation(1.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            date.dayOfWeek.getDisplayName(JTextStyle.SHORT, Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (offset == 0) StencillaTerracotta else StencillaGray,
                        )
                        Text(
                            date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (offset == 0) StencillaTerracotta else StencillaDarkGray,
                        )
                        Spacer(Modifier.height(6.dp))
                        // Placeholder [1] for outfit preview image
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (hasOutfit) StencillaTerracottaLight
                                    else StencillaLightGray
                                )
                                .border(
                                    1.dp,
                                    if (hasOutfit) StencillaTerracotta else StencillaLightBeige,
                                    RoundedCornerShape(10.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (hasOutfit) {
                                Icon(
                                    Icons.Outlined.Checkroom, null,
                                    tint = StencillaTerracotta,
                                    modifier = Modifier.size(22.dp),
                                )
                            } else {
                                Text("[1]", style = MaterialTheme.typography.labelSmall, color = StencillaGray)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        if (event != null) {
                            Text(
                                event.title, style = MaterialTheme.typography.labelSmall,
                                color = StencillaDarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Trip Planner Banner
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun TripPlannerBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaTerracotta),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.FlightTakeoff, null, tint = StencillaCardWhite, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Trip Planner", style = MaterialTheme.typography.titleSmall, color = StencillaCardWhite)
                Text(
                    "Pack smart — AI builds your travel capsule wardrobe.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StencillaCardWhite.copy(alpha = 0.8f),
                )
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = StencillaCardWhite)
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Event Card
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun EventCard(
    event: PlannerEventEntity,
    onClick: () -> Unit,
    onPlanOutfit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    val dateLabel = try {
        LocalDate.parse(event.eventDate).format(formatter)
    } catch (_: Exception) { event.eventDate }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Date pill
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StencillaTerracottaLight),
                contentAlignment = Alignment.Center,
            ) {
                Text(dateLabel, style = MaterialTheme.typography.labelSmall, color = StencillaTerracotta, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleSmall, color = StencillaDarkGray)
                val sub = listOfNotNull(event.occasion, event.dressCode, event.eventTime).joinToString(" · ")
                if (sub.isNotBlank()) {
                    Text(sub, style = MaterialTheme.typography.bodySmall, color = StencillaGray)
                }
                if (event.isTrip) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Outlined.FlightTakeoff, null, tint = StencillaTerracotta, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Trip" + if (event.tripEndDate != null) " → ${event.tripEndDate}" else "",
                            style = MaterialTheme.typography.labelSmall, color = StencillaTerracotta)
                    }
                }
            }
            TextButton(
                onClick = onPlanOutfit,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("Outfit", style = MaterialTheme.typography.labelSmall, color = StencillaTerracotta)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Helper composables
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, actionLabel: String?, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = StencillaDarkGray, modifier = Modifier.weight(1f))
        if (actionLabel != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) {
                Text(actionLabel, style = MaterialTheme.typography.labelSmall, color = StencillaTerracotta)
            }
        }
    }
}

@Composable
private fun EmptyStateCard(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, null, tint = StencillaLightGray, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.titleSmall, color = StencillaGray)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StencillaGray, textAlign = TextAlign.Center)
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Event Editor Bottom Sheet
// ──────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventEditorSheet(
    initial: PlannerEventEntity?,
    isTrip: Boolean,
    onSave: (String, String, String?, String?, String?, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var title    by remember { mutableStateOf(initial?.title ?: "") }
    var date     by remember { mutableStateOf(initial?.eventDate ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var time     by remember { mutableStateOf(initial?.eventTime ?: "") }
    var occasion by remember { mutableStateOf(initial?.occasion ?: "") }
    var dressCod by remember { mutableStateOf(initial?.dressCode ?: "") }
    var notes    by remember { mutableStateOf(initial?.notes ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = StencillaCardWhite) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(
                if (isTrip) "Plan a Trip" else if (initial == null) "Add Event" else "Edit Event",
                style = MaterialTheme.typography.titleMedium, color = StencillaDarkGray,
            )
            Spacer(Modifier.height(18.dp))

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StencillaTerracotta, focusedLabelColor = StencillaTerracotta,
            )
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors)
            Spacer(Modifier.height(10.dp))
            if (!isTrip) {
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (HH:MM, optional)") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors)
                Spacer(Modifier.height(10.dp))
            }
            OutlinedTextField(value = occasion, onValueChange = { occasion = it },
                label = { Text("Occasion (e.g. wedding, interview)") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = dressCod, onValueChange = { dressCod = it },
                label = { Text("Dress code (optional)") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(12.dp), colors = fieldColors)

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Text("Cancel", color = StencillaGray)
                }
                Button(
                    onClick = {
                        if (title.isNotBlank() && date.isNotBlank()) {
                            onSave(title, date, time.takeIf { it.isNotBlank() },
                                occasion.takeIf { it.isNotBlank() }, dressCod.takeIf { it.isNotBlank() },
                                notes.takeIf { it.isNotBlank() })
                        }
                    },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StencillaTerracotta),
                ) { Text("Save", color = StencillaCardWhite) }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Event Detail Sheet
// ──────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDetailSheet(
    event: PlannerEventEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onPlanOutfit: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = StencillaCardWhite) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium, color = StencillaDarkGray)
            Spacer(Modifier.height(8.dp))
            listOfNotNull(
                "Date" to event.eventDate,
                event.eventTime?.let { "Time" to it },
                event.occasion?.let { "Occasion" to it },
                event.dressCode?.let { "Dress code" to it },
                event.notes?.let { "Notes" to it },
                event.tripEndDate?.let { "Trip ends" to it },
            ).forEach { (label, value) ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("$label: ", style = MaterialTheme.typography.labelMedium, color = StencillaGray)
                    Text(value, style = MaterialTheme.typography.bodySmall, color = StencillaDarkGray)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDelete, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete") }
                Button(
                    onClick = onPlanOutfit, modifier = Modifier.weight(2f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StencillaTerracotta),
                ) {
                    Icon(Icons.Outlined.Checkroom, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Plan Outfit", color = StencillaCardWhite)
                }
            }
        }
    }
}
