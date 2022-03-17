package com.oyamo.decibel

import android.Manifest
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.oyamo.decibel.ui.theme.DecibelTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.log10


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }


    @Preview(showBackground = true)
    @Composable
    fun MainScreen() {
        DecibelTheme {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                MainActivityColumn()
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun MainActivityColumn() {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            var isRecording by remember { mutableStateOf(false) }
            var decibels by remember { mutableStateOf(0.00) }
            val composableScope = rememberCoroutineScope()

            val recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile("${applicationContext.filesDir}/test.3gp")
            }


            val audioPermisionState = rememberMultiplePermissionsState(
                listOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )


            val onRecordButtonClicked = {

                val allPermisionsGranted = audioPermisionState.allPermissionsGranted

                if (allPermisionsGranted) {
                    isRecording = !isRecording

                    composableScope.launch {
                        if (isRecording) {
                            try {
                                recorder.prepare()
                                recorder.start()

                            } catch (e: IllegalStateException) {
                                e.printStackTrace()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        } else {
                            decibels = 0.00
                        }

                        while (isRecording) {
                            delay(100)

                            val amplitude = recorder.maxAmplitude / (32767.00)
                            Log.d("Decibel", "Recording $amplitude")

                            decibels = (20 * Math.log10(amplitude)) + 100
                            if(decibels == Double.NEGATIVE_INFINITY) {
                                decibels = 0.00
                            }
                        }
                    }
                } else {
                    audioPermisionState.launchMultiplePermissionRequest()
                }
            }


            val titleText = String.format("%.2fdB", decibels)

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = titleText,
                fontSize = 120.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W900
            )

            val levelText = when (decibels) {
                in 0.0..85.00 -> {
                    "NORMAL"
                }

                in 85.00..110.00 -> {
                    "MODERATE"
                }

                else -> {
                    "LOUD"
                }
            }

            val levelImage = when (decibels) {
                in 0.0..85.00 -> {
                    R.drawable.normal_wave
                }

                in 85.00..110.00 -> {
                    R.drawable.moderate_wave
                }

                else -> {
                    R.drawable.too_loud
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.wrapContentHeight()
            ) {
                Card(elevation = 0.dp) {
                    Image(
                        painter = painterResource(id = levelImage),
                        contentDescription = "Drawable"
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(158.dp))
                        Card(elevation = 0.dp) {
                            Text(
                                text = levelText, Modifier.padding(16.dp),
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

            }

            OutlinedButton(
                onClick = { onRecordButtonClicked() },
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Blue)
            ) {
                val image = if (isRecording)
                    R.drawable.ic_baseline_stop_24 else R.drawable.ic_baseline_mic_24
                Icon(
                    painter = painterResource(id = image),
                    contentDescription = null
                )
            }
        }
    }
}