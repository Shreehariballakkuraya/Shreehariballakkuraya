package com.hari.docuvault

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult

class FolderViewActivity : AppCompatActivity() {

    private lateinit var fileRecyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private val fileList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_view)

        fileRecyclerView = findViewById(R.id.fileRecyclerView)
        fileRecyclerView.layoutManager = LinearLayoutManager(this)
        fileAdapter = FileAdapter(fileList)
        fileRecyclerView.adapter = fileAdapter

        val selectedFolder = intent.getStringExtra("folder") ?: "others"
        loadFiles(selectedFolder)
    }

    private fun loadFiles(folder: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("user_files/$userId/$folder")

        storageRef.listAll()
            .addOnSuccessListener { listResult: ListResult ->
                fileList.clear()
                for (item in listResult.items) {
                    fileList.add(item.path)
                }
                fileAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load files: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
