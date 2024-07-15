package com.hari.docuvault

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class FileListActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.file_list_activity)

        listView = findViewById(R.id.fileListView)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            loadFiles()
        }
    }

    private fun loadFiles() {
        val folderName = intent.getStringExtra("FOLDER_NAME") ?: return
        val folder = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), folderName)

        if (folder.exists() && folder.isDirectory) {
            val files = folder.listFiles()
            val fileNames = files?.map { it.name } ?: emptyList()
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames)
            listView.adapter = adapter
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFiles()
            } else {
                Toast.makeText(this, "Permission denied to read files", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
