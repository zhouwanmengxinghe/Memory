package com.example.myapplication1.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object FileUtils {
    
    fun getAppDirectory(context: Context): File {
        val appDir = File(context.filesDir, "memories")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        return appDir
    }
    
    fun getImagesDirectory(context: Context): File {
        val imagesDir = File(getAppDirectory(context), "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        return imagesDir
    }
    
    fun getAudioDirectory(context: Context): File {
        val audioDir = File(getAppDirectory(context), "audio")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        return audioDir
    }
    
    fun saveImageFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val file = File(getImagesDirectory(context), fileName)
            
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()
            
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    fun generateAudioFileName(): String {
        return "audio_${System.currentTimeMillis()}.3gp"
    }
    
    fun deleteFile(filePath: String?) {
        filePath?.let {
            try {
                val file = File(it)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun fileExists(filePath: String?): Boolean {
        return filePath?.let {
            File(it).exists()
        } ?: false
    }
}