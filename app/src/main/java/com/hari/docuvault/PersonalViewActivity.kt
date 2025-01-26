package com.hari.docuvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PersonalViewActivity : BaseFileViewActivity() {

    private lateinit var listView: ListView
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference
    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_view)

        // Initialize views
        listView = findViewById(R.id.listView)
        progressBar = findViewById(R.id.progressBar)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            storageRef = FirebaseStorage.getInstance().reference.child("user_files").child(userId).child("personal")
            databaseRef = FirebaseDatabase.getInstance().getReference("user_files").child(userId).child("personal")
            listFiles()
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listFiles() {
        showProgressBar()
        valueEventListener = databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<Pair<String, StorageReference>>()
                for (data in snapshot.children) {
                    val metadata = data.getValue(PersonalMetadata::class.java)
                    val fileName = metadata?.fileName ?: "Unknown"
                    val fileRef = storageRef.child(fileName)
                    val metadataText = "Name: $fileName, Type: ${metadata?.documentType ?: "N/A"}, Expiry: ${metadata?.expiryDate ?: "N/A"}"
                    items.add(Pair(metadataText, fileRef))
                }

                val adapter = object : ArrayAdapter<Pair<String, StorageReference>>(this@PersonalViewActivity, R.layout.item_list_file, items) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_list_file, parent, false)
                        val metadataTextView: TextView = view.findViewById(R.id.metadataTextView)
                        val documentTypeTextView: TextView = view.findViewById(R.id.documentTypeTextView)
                        val expiryDateTextView: TextView = view.findViewById(R.id.expiryDateTextView)
                        val downloadButton: Button = view.findViewById(R.id.downloadButton)
                        val fileIconImageView: ImageView = view.findViewById(R.id.fileIconImageView)

                        val (metadata, fileRef) = getItem(position) ?: return view
                        val metadataObj = snapshot.children.elementAt(position).getValue(PersonalMetadata::class.java)
                        metadataTextView.text = metadata
                        documentTypeTextView.text = "Type: ${metadataObj?.documentType ?: "N/A"}"
                        expiryDateTextView.text = "Expiry: ${metadataObj?.expiryDate ?: "N/A"}"

                        val fileExtension = fileRef.name.substringAfterLast('.', "")
                        val iconResId = when (fileExtension.lowercase()) {
                            "pdf" -> R.drawable.ic_pdf
                            "jpg", "jpeg", "png" -> R.drawable.ic_image
                            "doc", "docx" -> R.drawable.ic_doc
                            "xls", "xlsx" -> R.drawable.ic_xls
                            else -> R.drawable.ic_file
                        }
                        fileIconImageView.setImageResource(iconResId)

                        downloadButton.setOnClickListener {
                            checkStoragePermissionAndDownload(fileRef)
                        }
                        return view
                    }
                }
                listView.adapter = adapter
                hideProgressBar()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PersonalViewActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                hideProgressBar()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        valueEventListener?.let { databaseRef.removeEventListener(it) }
    }
}
