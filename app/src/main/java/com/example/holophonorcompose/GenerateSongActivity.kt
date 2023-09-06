package com.example.holophonorcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.holophonorcompose.ui.theme.BackgroundImage

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

    var selectedTab by remember { mutableStateOf(0) } // Track the selected tab index

    val genreOptions = Genre.values().toList()
    val moodOptions = Mood.values().toList()
    val instrumentOptions = Instrument.values().toList()
    val tempoOptions = Tempo.values().toList()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab, // Use the selectedTab state
            backgroundColor = Color.DarkGray,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                text = { Text("Genres", color = Color.White ) },
                selected = selectedTab == 0, // Check if this tab is selected
                onClick = { selectedTab = 0 }
            )
            Tab(
                text = { Text("Moods", color = Color.White) },
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            )
            Tab(
                text = { Text("Instruments", color = Color.White) },
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 }
            )
            Tab(
                text = { Text("Tempo", color = Color.White) },
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 }
            )
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

            3 -> {
                TempoTab(
                    tempoOptions = tempoOptions,
                    selectedTempo = selectedTempo,
                    onTempoSelected = { selectedTempo = it }
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
            .fillMaxSize()
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
            .fillMaxSize()
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
            .fillMaxSize()
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

@Composable
private fun TempoTab(
    tempoOptions: List<Tempo>,
    selectedTempo: Tempo?,
    onTempoSelected: (Tempo?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Create a dropdown menu for selecting tempo
        var expanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .clickable { expanded = true }
        ) {
            BasicTextField(
                value = selectedTempo?.name ?: "Select Tempo",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                tempoOptions.forEach { tempo ->
                    DropdownMenuItem(
                        onClick = {
                            onTempoSelected(tempo)
                            expanded = false
                        }
                    ) {
                        Text(text = tempo.name, color = Color.White)
                    }
                }
            }
        }
    }
}


fun GenerateSong() {
    val apiClient = ApiClient()

    val textToGenerateAudio = "This is the text to convert to audio."

    val callGenerateAudio = apiClient.generateAudioFromText(textToGenerateAudio)
    callGenerateAudio.enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                // Handle the successful response, which should contain the generated audio data
                val responseBody = response.body()
                val generatedAudioData = responseBody?.bytes()

                // Save the received audio data to a file or process it as needed
            } else {
                // Handle the API error response for generating audio
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            // Handle network or other failures
        }
    })
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HolophonorTheme {
        GenerateSongUI()
    }
}