package com.example.holophonorcompose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.*

import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale

import com.example.holophonorcompose.ui.theme.HolophonorTheme


import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HolophonorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    MainScreen(context)
                }
            }
        }
    }
}

@Composable
fun BackgroundImage(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(           // Replace with your image id
                painterResource(id = R.drawable.background_purple_vuluptous),
                contentScale = ContentScale.FillBounds)
    ) {
        content()
    }
}

@Composable
fun MainScreen(context: Context) {
    BackgroundImage() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, GenerateSongActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text("Generate Song", textAlign=TextAlign.Center)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {  },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text("Upload Song", textAlign=TextAlign.Center)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(context, RecordActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text("Record", textAlign=TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start
            )
            {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(0.8f) // Make the Box fill the available space
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.waveform3),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 69.dp) // Adjust the bottom padding as needed
                    )
                    Text(
                        text = "my_song.mp3",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.Center) // Center the text within the Box
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.playbutton),
                    contentDescription = "Image",
                    modifier = Modifier
                        .size(100.dp)
                        .offset(-60.dp)
//                    .border(2.dp, Color.Black) // Apply a border with width and color
                        .fillMaxWidth()
                        .padding(bottom = 8.dp) // Adjust the bottom padding as needed
                )
            }


            Button(
                onClick = { /* Handle Create Slideshow button click */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Create AI Generated Video", color = Color.White, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Add the image at the middle bottom
            Image(
                painter = painterResource(id = R.drawable.coolimg),
                contentDescription = "Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp) // Adjust the bottom padding as needed
                    .align(Alignment.CenterHorizontally) // Center horizontally
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    HolophonorTheme {
        val context = LocalContext.current
        MainScreen(context)
    }
}
