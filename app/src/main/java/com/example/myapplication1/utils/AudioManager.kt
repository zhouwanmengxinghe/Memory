package com.example.myapplication1.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class AudioManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var _isRecording = false
    private var _isPlaying = false

    fun startRecording(outputFile: String): Boolean {
        return try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(outputFile)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                
                try {
                    prepare()
                    start()
                    _isRecording = true
                    true
                } catch (e: IOException) {
                    e.printStackTrace()
                    false
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun stopRecording() {
        if (_isRecording) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                _isRecording = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun playAudio(audioPath: String, onCompletion: () -> Unit = {}) {
        withContext(Dispatchers.IO) {
            try {
                stopAudio()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioPath)
                    setOnCompletionListener {
                        _isPlaying = false
                        onCompletion()
                    }
                    prepare()
                    start()
                    _isPlaying = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopAudio() {
        if (_isPlaying) {
            try {
                mediaPlayer?.apply {
                    stop()
                    release()
                }
                mediaPlayer = null
                _isPlaying = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isRecording(): Boolean = _isRecording
    fun isPlaying(): Boolean = _isPlaying

    fun release() {
        stopRecording()
        stopAudio()
    }

    fun getAudioDuration(audioPath: String): Long {
        return try {
            val mp = MediaPlayer()
            mp.setDataSource(audioPath)
            mp.prepare()
            val duration = mp.duration.toLong()
            mp.release()
            duration
        } catch (e: Exception) {
            0L
        }
    }
}