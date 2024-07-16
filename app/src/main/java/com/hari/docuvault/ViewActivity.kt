package com.hari.docuvault

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import com.hari.docuvault.FileAdapter


class ViewActivity : AppCompatActivity() {

    private val REQUEST_PERMISSIONS = 1
    private val FOLDER_NAME = "docuvault"  // Define the folder name here

    private lateinit var fileRecyclerView: RecyclerView
    private lateinit var fileAdapter:FileAdapter
    private val fileList = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        fileRecyclerView = findViewById(R.id.fileRecyclerView)
        fileRecyclerView.layoutManager = LinearLayoutManager(this)
        fileAdapter = FileAdapter(fileList)
        fileRecyclerView.adapter = fileAdapter

        if (!hasStoragePermissions()) {
            requestStoragePermissions()
        } else {
            createAndShowFiles()
        }
    }

    private fun hasStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createAndShowFiles()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createAndShowFiles() {
        val folderPath = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), FOLDER_NAME)
        if (!folderPath.exists()) {
            folderPath.mkdirs()  // Create folder if it does not exist
        }

        // Get list of files in the directory
        fileList.clear()
        fileList.addAll(folderPath.listFiles() ?: emptyArray())
        fileAdapter.notifyDataSetChanged()
    }
}
