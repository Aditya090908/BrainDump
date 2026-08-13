package com.example.braindump

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.braindump.ui.theme.BRAINDUMPTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                1001
            )
        }

        setContent {

            BrainDumpApp(
                database =
                    DatabaseProvider.getDatabase(
                        this@MainActivity
                    )
            )
        }
    }
}


// ============================================================
// MAIN APP
// ============================================================

@Composable
fun BrainDumpApp(
    database: BrainDumpDatabase
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current
    val testReminder = Reminder(
        id = 9999,
        thoughtId = 0,
        title = "Test Meeting",
        originalText = "This is a Brain Dump test reminder.",
        reminderTime = System.currentTimeMillis() + 60_000L
    )

    val preferences =
        remember {

            context.getSharedPreferences(
                "brain_dump_settings",
                Context.MODE_PRIVATE
            )
        }

    var darkMode by remember {

        mutableStateOf(
            preferences.getBoolean(
                "dark_mode",
                false
            )
        )
    }

    var selectedScreen by remember {
        mutableStateOf(0)
    }

    BRAINDUMPTheme(
        darkTheme = darkMode
    ) {

        Scaffold(

            modifier =
                Modifier.fillMaxSize(),

            bottomBar = {

                NavigationBar {

                    NavigationBarItem(

                        selected =
                            selectedScreen == 0,

                        onClick = {
                            selectedScreen = 0
                        },

                        icon = {
                            Text("🏠")
                        },

                        label = {
                            Text("Home")
                        }
                    )


                    NavigationBarItem(

                        selected =
                            selectedScreen == 1,

                        onClick = {
                            selectedScreen = 1
                        },

                        icon = {
                            Text("🧠")
                        },

                        label = {
                            Text("Thoughts")
                        }
                    )


                    NavigationBarItem(

                        selected =
                            selectedScreen == 2,

                        onClick = {
                            selectedScreen = 2
                        },

                        icon = {
                            Text("⚙️")
                        },

                        label = {
                            Text("Settings")
                        }
                    )
                }
            }

        ) { innerPadding ->

            when (selectedScreen) {

                0 -> {

                    HomeScreen(

                        modifier =
                            Modifier.padding(
                                innerPadding
                            ),

                        database =
                            database,

                        darkMode =
                            darkMode,

                        onThemeClick = {

                            darkMode =
                                !darkMode

                            preferences.edit()
                                .putBoolean(
                                    "dark_mode",
                                    darkMode
                                )
                                .apply()
                        }
                    )
                }


                1 -> {

                    ThoughtsScreen(

                        modifier =
                            Modifier.padding(
                                innerPadding
                            ),

                        database =
                            database
                    )
                }


                2 -> {

                    SettingsScreen(

                        modifier =
                            Modifier.padding(
                                innerPadding
                            ),

                        darkMode =
                            darkMode,

                        onThemeChanged = {

                            darkMode =
                                it

                            preferences.edit()
                                .putBoolean(
                                    "dark_mode",
                                    it
                                )
                                .apply()
                        }
                    )
                }
            }
        }
    }
}


// ============================================================
// HOME SCREEN
// ============================================================

@Composable
fun HomeScreen(
    modifier: Modifier,
    database: BrainDumpDatabase,
    darkMode: Boolean,
    onThemeClick: () -> Unit
) {

    var brainDumpText by remember {
        mutableStateOf("")
    }

    var savedMessage by remember {
        mutableStateOf(false)
    }

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val preferences =
        remember {

            context.getSharedPreferences(
                "brain_dump_settings",
                Context.MODE_PRIVATE
            )
        }

    val coroutineScope =
        rememberCoroutineScope()


    Column(

        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
            .navigationBarsPadding(),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {


        // HEADER

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column {

                Text(

                    text =
                        "🧠 Brain Dump",

                    fontSize =
                        26.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Text(

                    text =
                        "Clear your mind. Capture your thoughts.",

                    fontSize =
                        14.sp,

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }


            IconButton(
                onClick =
                    onThemeClick
            ) {

                Text(

                    text =
                        if (darkMode) {
                            "☀️"
                        } else {
                            "🌙"
                        },

                    fontSize =
                        22.sp
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        // WRITING BOX

        Box(

            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(

                    color =
                        MaterialTheme.colorScheme
                            .surfaceVariant,

                    shape =
                        RoundedCornerShape(18.dp)
                )
        ) {


            BasicTextField(

                value =
                    brainDumpText,

                onValueChange = {

                    brainDumpText =
                        it

                    savedMessage =
                        false
                },

                textStyle =
                    TextStyle(

                        fontSize =
                            16.sp,

                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    ),

                modifier = Modifier
                    .fillMaxWidth()
                    .align(
                        Alignment.BottomStart
                    )
                    .padding(

                        start =
                            16.dp,

                        end =
                            16.dp,

                        bottom =
                            15.dp
                    ),

                maxLines =
                    7
            )


            if (brainDumpText.isEmpty()) {

                Text(

                    text =
                        "What's on your mind?",

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,

                    fontSize =
                        15.sp,

                    modifier = Modifier
                        .align(
                            Alignment.BottomStart
                        )
                        .padding(

                            start =
                                16.dp,

                            bottom =
                                15.dp
                        )
                )
            }


            Box(

                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(
                        Alignment.BottomCenter
                    )
                    .background(
                        MaterialTheme.colorScheme.primary
                    )
            )

        }


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        // SAVE

        Button(
            onClick = {

                if (brainDumpText.isNotBlank()) {

                    coroutineScope.launch {

                        val dump = Dump(
                            text = brainDumpText.trim()
                        )

                        val dumpId =
                            database.dumpDao().insertDump(dump)

                        val parsedReminder =
                            ReminderParser.parse(
                                brainDumpText.trim()
                            )

                        if (parsedReminder != null) {

                            val reminder =
                                Reminder(
                                    thoughtId = dumpId.toInt(),
                                    title = parsedReminder.title,
                                    originalText = brainDumpText.trim(),
                                    reminderTime =
                                        parsedReminder.reminderTime
                                )

                            val reminderId =
                                database.reminderDao()
                                    .insertReminder(reminder)

                            val savedReminder =
                                reminder.copy(
                                    id = reminderId.toInt()
                                )

                            val scheduled =
                                ReminderScheduler.schedule(
                                    context,
                                    savedReminder
                                )

                            if (!scheduled) {

                                ReminderScheduler
                                    .openExactAlarmSettings(
                                        context
                                    )
                            }
                        }

                        brainDumpText = ""

                        savedMessage = true
                    }
                }

            }
        ) {
            Text("＋ Save Thought")
        }


        if (savedMessage) {

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            Text(

                text =
                    "✓ Thought saved",

                color =
                    MaterialTheme.colorScheme.primary,

                fontWeight =
                    FontWeight.SemiBold
            )
        }


        // EMPTY HOME AREA

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center
        ) {

            if (!savedMessage) {

                Text(
                    text =
                        "🧠",

                    fontSize =
                        42.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(

                    text =
                        "Your thoughts are safe here.",

                    fontSize =
                        16.sp,

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}


// ============================================================
// THOUGHTS SCREEN
// ============================================================

@Composable
fun ThoughtsScreen(
    modifier: Modifier,
    database: BrainDumpDatabase
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val preferences =
        remember {

            context.getSharedPreferences(
                "brain_dump_settings",
                Context.MODE_PRIVATE
            )
        }


    var savedDumps by remember {

        mutableStateOf<List<Dump>>(
            emptyList()
        )
    }


    var searchText by remember {
        mutableStateOf("")
    }


    var selectedFilter by remember {
        mutableStateOf("ALL")
    }


    var favoriteIds by remember {

        mutableStateOf(
            loadFavoriteIds(
                preferences
            )
        )
    }


    var editingDump by remember {
        mutableStateOf<Dump?>(null)
    }


    var deletingDump by remember {
        mutableStateOf<Dump?>(null)
    }


    val coroutineScope =
        rememberCoroutineScope()


    LaunchedEffect(Unit) {

        savedDumps =
            database.dumpDao()
                .getAllDumps()

        favoriteIds =
            loadFavoriteIds(
                preferences
            )
    }


    val filteredDumps =
        savedDumps.filter { dump ->

            val matchesSearch =
                dump.text.contains(
                    searchText,
                    ignoreCase = true
                )

            val matchesFavorite =
                selectedFilter == "ALL" ||
                        favoriteIds.contains(
                            dump.id
                        )

            matchesSearch &&
                    matchesFavorite
        }


    Column(

        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
            .navigationBarsPadding()
    ) {


        Text(

            text =
                "🧠 Your Thoughts",

            fontSize =
                26.sp,

            fontWeight =
                FontWeight.Bold
        )


        Spacer(
            modifier =
                Modifier.height(5.dp)
        )


        Text(

            text =
                "Everything you've captured.",

            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(18.dp)
        )


        // SEARCH

        OutlinedTextField(

            value =
                searchText,

            onValueChange = {
                searchText =
                    it
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine =
                true,

            label = {
                Text(
                    "Search thoughts"
                )
            },

            placeholder = {
                Text(
                    "Type to search..."
                )
            }
        )


        Spacer(
            modifier =
                Modifier.height(14.dp)
        )


        // FILTERS

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            FilterButton(

                text =
                    "All",

                selected =
                    selectedFilter ==
                            "ALL",

                onClick = {
                    selectedFilter =
                        "ALL"
                },

                modifier =
                    Modifier.weight(1f)
            )


            FilterButton(

                text =
                    "⭐ Favorites",

                selected =
                    selectedFilter ==
                            "FAVORITES",

                onClick = {
                    selectedFilter =
                        "FAVORITES"
                },

                modifier =
                    Modifier.weight(1f)
            )
        }


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        if (filteredDumps.isEmpty()) {


            Column(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(

                    text =

                        if (
                            selectedFilter ==
                            "FAVORITES"
                        ) {

                            "No favorite thoughts yet."

                        } else if (
                            searchText.isNotEmpty()
                        ) {

                            "No matching thoughts."

                        } else {

                            "Nothing here yet."
                        },

                    fontSize =
                        19.sp,

                    fontWeight =
                        FontWeight.SemiBold
                )


                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )


                Text(

                    text =

                        if (
                            selectedFilter ==
                            "FAVORITES"
                        ) {

                            "Tap ☆ on a thought to save it here."

                        } else if (
                            searchText.isNotEmpty()
                        ) {

                            "Try another search."

                        } else {

                            "Your saved thoughts will appear here."
                        },

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

        } else {


            LazyColumn(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(

                    items =
                        filteredDumps,

                    key = {
                        it.id
                    }

                ) { dump ->


                    ThoughtCard(

                        dump =
                            dump,

                        isFavorite =
                            favoriteIds.contains(
                                dump.id
                            ),

                        timestamp =
                            getTimestamp(
                                preferences,
                                dump.id
                            ),

                        onFavorite = {

                            val updated =
                                favoriteIds
                                    .toMutableSet()


                            if (
                                updated.contains(
                                    dump.id
                                )
                            ) {

                                updated.remove(
                                    dump.id
                                )

                            } else {

                                updated.add(
                                    dump.id
                                )
                            }


                            favoriteIds =
                                updated


                            saveFavoriteIds(

                                preferences,

                                updated
                            )
                        },


                        onEdit = {

                            editingDump =
                                dump
                        },


                        onDelete = {

                            deletingDump =
                                dump
                        }
                    )
                }
            }
        }
    }


    // EDIT DIALOG

    editingDump?.let { dump ->

        EditThoughtDialog(

            dump =
                dump,

            onDismiss = {

                editingDump =
                    null
            },

            onSave = { newText ->

                coroutineScope.launch {

                    database.dumpDao()
                        .updateDump(

                            dump.copy(
                                text =
                                    newText.trim()
                            )
                        )

                    savedDumps =
                        database.dumpDao()
                            .getAllDumps()

                    editingDump =
                        null
                }
            }
        )
    }


    // DELETE CONFIRMATION

    deletingDump?.let { dump ->

        DeleteConfirmationDialog(

            onDismiss = {

                deletingDump =
                    null
            },

            onConfirm = {

                coroutineScope.launch {

                    database.dumpDao()
                        .deleteDump(
                            dump
                        )


                    // Remove favorite

                    val updated =
                        favoriteIds
                            .toMutableSet()

                    updated.remove(
                        dump.id
                    )

                    favoriteIds =
                        updated


                    saveFavoriteIds(

                        preferences,

                        updated
                    )


                    // Remove timestamp

                    removeTimestamp(

                        preferences,

                        dump.id
                    )


                    savedDumps =
                        database.dumpDao()
                            .getAllDumps()


                    deletingDump =
                        null
                }
            }
        )
    }
}


// ============================================================
// THOUGHT CARD
// ============================================================

@Composable
fun ThoughtCard(
    dump: Dump,
    isFavorite: Boolean,
    timestamp: Long?,
    onFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    MaterialTheme.colorScheme
                        .surfaceVariant
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(

            modifier =
                Modifier.padding(16.dp)
        ) {


            // TOP ROW

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.Top
            ) {

                Text(

                    text =
                        dump.text,

                    modifier =
                        Modifier.weight(1f),

                    fontSize =
                        16.sp,

                    lineHeight =
                        23.sp
                )


                IconButton(

                    onClick =
                        onFavorite
                ) {

                    Text(

                        text =

                            if (
                                isFavorite
                            ) {

                                "⭐"

                            } else {

                                "☆"
                            },

                        fontSize =
                            25.sp
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            // DATE

            Text(

                text =
                    formatTimestamp(
                        timestamp
                    ),

                fontSize =
                    12.sp,

                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            // ACTIONS

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.End
            ) {

                TextButton(
                    onClick =
                        onEdit
                ) {

                    Text(
                        "Edit"
                    )
                }


                TextButton(
                    onClick =
                        onDelete
                ) {

                    Text(
                        "Delete"
                    )
                }
            }
        }
    }
}


// ============================================================
// FILTER BUTTON
// ============================================================

@Composable
fun FilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                if (text.contains("Favorites")) {
                    Color(0xFFFFC107)
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (selected && text.contains("Favorites")) {
                Color.Black
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Text(text)
    }
}


// ============================================================
// EDIT DIALOG
// ============================================================

@Composable
fun EditThoughtDialog(
    dump: Dump,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {

    var editedText by remember {

        mutableStateOf(
            dump.text
        )
    }


    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(
                "Edit thought"
            )
        },

        text = {

            OutlinedTextField(

                value =
                    editedText,

                onValueChange = {

                    editedText =
                        it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                minLines =
                    4,

                label = {

                    Text(
                        "Your thought"
                    )
                }
            )
        },

        confirmButton = {

            TextButton(

                onClick = {

                    if (
                        editedText
                            .isNotBlank()
                    ) {

                        onSave(
                            editedText
                        )
                    }
                }
            ) {

                Text(
                    "Save"
                )
            }
        },

        dismissButton = {

            TextButton(

                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancel"
                )
            }
        }
    )
}


// ============================================================
// DELETE CONFIRMATION
// ============================================================

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(
                "Delete thought?"
            )
        },

        text = {

            Text(
                "This thought will be permanently removed."
            )
        },

        confirmButton = {

            TextButton(
                onClick =
                    onConfirm
            ) {

                Text(
                    "Delete"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancel"
                )
            }
        }
    )
}


// ============================================================
// SETTINGS
// ============================================================

@Composable
fun SettingsScreen(
    modifier: Modifier,
    darkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {

    Column(

        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .navigationBarsPadding()
    ) {


        Text(

            text =
                "⚙️ Settings",

            fontSize =
                26.sp,

            fontWeight =
                FontWeight.Bold
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        Text(

            text =
                "Customize your Brain Dump experience.",

            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        // THEME

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    18.dp
                )
        ) {

            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(

                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(

                        text =
                            "Dark mode",

                        fontSize =
                            18.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Text(

                        text =

                            if (darkMode) {

                                "Dark theme is enabled"

                            } else {

                                "Light theme is enabled"
                            },

                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }


                Switch(

                    checked =
                        darkMode,

                    onCheckedChange =
                        onThemeChanged
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        // ABOUT

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    18.dp
                )
        ) {

            Column(

                modifier =
                    Modifier.padding(18.dp)
            ) {

                Text(

                    text =
                        "Brain Dump",

                    fontSize =
                        18.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )


                Text(
                    "Your personal space to clear your mind."
                )


                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )


                Text(
                    "Version 2.0"
                )


                Text(
                    "Built with Kotlin + Jetpack Compose."
                )
            }
        }
    }
}


// ============================================================
// FAVORITE STORAGE
// ============================================================

fun loadFavoriteIds(
    preferences:
    android.content.SharedPreferences
): Set<Int> {

    return preferences
        .getStringSet(
            "favorite_dump_ids",
            emptySet()
        )
        ?.mapNotNull {

            it.toIntOrNull()

        }
        ?.toSet()
        ?: emptySet()
}


fun saveFavoriteIds(
    preferences:
    android.content.SharedPreferences,

    favoriteIds:
    Set<Int>
) {

    preferences.edit()
        .putStringSet(

            "favorite_dump_ids",

            favoriteIds
                .map {
                    it.toString()
                }
                .toSet()
        )
        .apply()
}


// ============================================================
// TIMESTAMP STORAGE
// ============================================================

fun saveTimestamp(
    preferences:
    android.content.SharedPreferences,

    id: Long,

    timestamp: Long
) {

    preferences.edit()
        .putLong(
            "timestamp_$id",
            timestamp
        )
        .apply()
}


fun getTimestamp(
    preferences:
    android.content.SharedPreferences,

    id: Int
): Long? {

    val value =
        preferences.getLong(
            "timestamp_$id",
            0L
        )

    return if (
        value == 0L
    ) {
        null
    } else {
        value
    }
}


fun removeTimestamp(
    preferences:
    android.content.SharedPreferences,

    id: Int
) {

    preferences.edit()
        .remove(
            "timestamp_$id"
        )
        .apply()
}


// ============================================================
// DATE FORMAT
// ============================================================

fun formatTimestamp(
    timestamp: Long?
): String {

    if (timestamp == null) {
        return "Saved earlier"
    }


    val date =
        Date(timestamp)


    val today =
        SimpleDateFormat(
            "yyyyMMdd",
            Locale.getDefault()
        ).format(
            Date()
        )


    val dateValue =
        SimpleDateFormat(
            "yyyyMMdd",
            Locale.getDefault()
        ).format(
            date
        )


    return when {

        today == dateValue -> {

            "Today • " +
                    SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(date)
        }


        else -> {

            SimpleDateFormat(
                "dd MMM yyyy • hh:mm a",
                Locale.getDefault()
            ).format(date)
        }
    }
}