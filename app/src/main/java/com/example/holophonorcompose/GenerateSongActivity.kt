package com.example.holophonorcompose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.holophonorcompose.API.ApiClient
import com.example.holophonorcompose.ui.theme.HolophonorTheme
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import com.example.holophonorcompose.ui.theme.BackgroundImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class GenerateSongActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HolophonorTheme {
                BackgroundImage {
                    GenerateSongUI()
                }
            }
        }
    }
}

enum class Genre {
    Pop, Rock, HipHop, Rap, RnB, Country, ElectronicDance, Reggae, Jazz, Classical, Blues, Metal, IndieAlternative, Latin, KPop, Gospel, Folk
}

enum class Mood {
    Happy, Hopeful, Energetic, Sad, Calm, Excited, Relaxed, Angry, Romantic, Melancholy, Confident, Nostalgic, Playful, Thoughtful, Mysterious, Peaceful, Empowered, Dreamy, Aggressive, Joyful
}

enum class Instrument {
    Piano, Violin, Flute, Guitar, Cello, Saxophone, Trumpet, Clarinet, Drums
}

enum class Tempo {
    Slow, Normal, Fast
}

@Composable
fun GenerateSongUI() {
    var selectedGenres by remember { mutableStateOf(setOf<Genre>()) }
    var selectedMoods by remember { mutableStateOf(setOf<Mood>()) }
    var selectedInstruments by remember { mutableStateOf(setOf<Instrument>()) }
    var selectedTempo by remember { mutableStateOf<Tempo?>(null) }

    var selectedTab by remember { mutableIntStateOf(0) } // Track the selected tab index

    val genreOptions = Genre.values().toList()
    val moodOptions = Mood.values().toList()
    val instrumentOptions = Instrument.values().toList()
    val tempoOptions = Tempo.values().toList()

    val headerFontSize = 15.sp
    val tabOptions = listOf("Genres", "Moods", "Instruments")
    var isDropdownExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = MaterialTheme.colors.secondary,
            contentColor = Color.White,
        ) {
            tabOptions.forEachIndexed { index, title ->
                Tab(
                    text = { Text(
                        text = title,
                        fontSize = headerFontSize,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.zIndex(1000000f)
                    )},
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        isDropdownExpanded = false // Close the dropdown when a tab is clicked
                    },
                    modifier = Modifier
                        .height(50.dp)
                )
            }

            // Dropdown Tempo Tab
            Box(
                modifier = Modifier.clickable { isDropdownExpanded = !isDropdownExpanded },
                contentAlignment = Alignment.Center
            ) {
                Text("Tempo", fontSize = headerFontSize)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "downArrow",
                    modifier = Modifier
                        .padding(top = (headerFontSize.value * 1.5).dp)
                        .size(headerFontSize.value.dp)
                )
                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                ) {
                    tempoOptions.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            onClick = {
                                isDropdownExpanded = false
                                selectedTempo = option
                            }
                        ) {
                            Text(text = option.toString())
                        }
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
                GenreTab(
                    genreOptions = genreOptions,
                    selectedGenres = selectedGenres,
                    onGenreSelected = { selectedGenres = it }
                )
            }

            1 -> {
                MoodTab(
                    moodOptions = moodOptions,
                    selectedMoods = selectedMoods,
                    onMoodSelected = { selectedMoods = it }
                )
            }

            2 -> {
                InstrumentTab(
                    instrumentOptions = instrumentOptions,
                    selectedInstruments = selectedInstruments,
                    onInstrumentSelected = { selectedInstruments = it }
                )
            }
        }


        // Display the selected options
        SelectedOptionsList(
            selectedGenres = selectedGenres,
            selectedMoods = selectedMoods,
            selectedInstruments = selectedInstruments,
            selectedTempo = selectedTempo
        )
    }
}


@Composable
fun SelectedOptionsList(
    selectedGenres: Set<Genre>,
    selectedMoods: Set<Mood>,
    selectedInstruments: Set<Instrument>,
    selectedTempo: Tempo?
) {
    val context = LocalContext.current
    Text("Selected Options:", color = Color.White)
    Spacer(modifier = Modifier.height(8.dp))

    // Display selected genres
    if (selectedGenres.isNotEmpty()) {
        Text("Genres: ${selectedGenres.joinToString(", ")}", color = Color.White)
    }

    // Display selected moods
    if (selectedMoods.isNotEmpty()) {
        Text("Moods: ${selectedMoods.joinToString(", ")}", color = Color.White)
    }

    // Display selected instruments
    if (selectedInstruments.isNotEmpty()) {
        Text("Instruments: ${selectedInstruments.joinToString(", ")}", color = Color.White)
    }

    // Display selected tempo
    selectedTempo?.let {
        Text("Tempo: ${it.name}", color = Color.White)
    }

    Row(
        modifier = Modifier
            .padding(top = 50.dp),
        horizontalArrangement = Arrangement.Center
    )
    {
        Button(
            onClick = {
                val prompt = "Genres: " + selectedGenres.joinToString(", ") +
                        " Moods: " + selectedMoods.joinToString(", ") +
                        " Instruments: " + selectedInstruments.joinToString(", ") +
                        " Tempo: " + selectedTempo.toString()
                generateSong(prompt, context) { uri ->
                    if (uri != null) {
                        Log.d("SUCCESS", "LAUNCHING MAIN ACTIVITY WITH SONG URI")
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("audioFileUri", uri.toString())
                        context.startActivity(intent)
                    } else {
                        Log.d("ERROR", "URI WAS NULL")
                    }
                }
            }
        )
        {
            Text("Generate Song")
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenreTab(
    genreOptions: List<Genre>,
    selectedGenres: Set<Genre>,
    onGenreSelected: (Set<Genre>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Select Genres:", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        // Create checkboxes for each genre using FlowRow
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            genreOptions.forEach { genre ->
                val isSelected = selectedGenres.contains(genre)
                Row(
                    modifier = Modifier.selectable(
                        selected = isSelected,
                        onClick = {
                            val updatedGenres = if (isSelected) {
                                selectedGenres - genre
                            } else {
                                selectedGenres + genre
                            }
                            onGenreSelected(updatedGenres)
                        }
                    )
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null // Handled by Row onClick
                    )
                    Text(
                        text = genre.name,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .padding(start = 4.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodTab(
    moodOptions: List<Mood>,
    selectedMoods: Set<Mood>,
    onMoodSelected: (Set<Mood>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Select Moods:", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        // Create checkboxes for each mood using FlowRow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            moodOptions.forEach { mood ->
                val isSelected = selectedMoods.contains(mood)
                Row(
                    modifier = Modifier.selectable(
                        selected = isSelected,
                        onClick = {
                            val updatedMoods = if (isSelected) {
                                selectedMoods - mood
                            } else {
                                selectedMoods + mood
                            }
                            onMoodSelected(updatedMoods)
                        }
                    )
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null // Handled by Row onClick
                    )
                    Text(
                        text = mood.name,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .padding(start = 4.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InstrumentTab(
    instrumentOptions: List<Instrument>,
    selectedInstruments: Set<Instrument>,
    onInstrumentSelected: (Set<Instrument>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Select Instruments:", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        // Create checkboxes for each instrument using FlowRow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            instrumentOptions.forEach { instrument ->
                val isSelected = selectedInstruments.contains(instrument)
                Row(
                    modifier = Modifier.selectable(
                        selected = isSelected,
                        onClick = {
                            val updatedInstruments = if (isSelected) {
                                selectedInstruments - instrument
                            } else {
                                selectedInstruments + instrument
                            }
                            onInstrumentSelected(updatedInstruments)
                        }
                    )
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null // Handled by Row onClick
                    )
                    Text(
                        text = instrument.name,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .padding(start = 4.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}


fun generateSong(textToGenerateAudio: String, context : Context, callback: (Uri?) -> Unit) {
    val apiClient = ApiClient()
    var uri: Uri? = null

    val callGenerateAudio = apiClient.generateAudioFromText(textToGenerateAudio)
    callGenerateAudio.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                Log.d("info", "SUCCESS NO WAY HOMIE")
                val responseBody = response.body()
                if (responseBody != null) {
                    uri = saveResponseBodyToTempFile(context, responseBody)
                    callback(uri) // Update the Uri using the callback
                }
            } else {
                val responseBody = response.body()
                Log.d("info", "FAILURE: " + responseBody.toString())
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            // Handle network or other failures
        }
    })
}

fun saveResponseBodyToTempFile(context: Context, responseBody: ResponseBody): Uri {
    val tempDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
    val tempFile = File(tempDir, "generated_song.mp3")
    try {
        val inputStream = responseBody.byteStream()
        val outputStream = FileOutputStream(tempFile)
        val buffer = ByteArray(4096)
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush() // Add this line
        outputStream.close()
        inputStream.close()
    } catch (e: IOException) {
        throw e
    }
    Log.d("URI", tempFile.toURI().toString())
    return tempFile.toUri()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HolophonorTheme {
        GenerateSongUI()
    }
}