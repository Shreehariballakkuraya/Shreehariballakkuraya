package com.hari.docuvault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val vehicleButton: Button = findViewById(R.id.vehicleButton)
        val personalButton: Button = findViewById(R.id.personalButton)
        val otherButton: Button = findViewById(R.id.otherButton)

        vehicleButton.setOnClickListener {
            openFolder("vehicle")
        }

        personalButton.setOnClickListener {
            openFolder("personal")
        }

        otherButton.setOnClickListener {
            openFolder("others")
        }
    }

    private fun openFolder(folder: String) {
        val intent = Intent(this, FolderViewActivity::class.java)
        intent.putExtra("folder", folder)
        startActivity(intent)
    }
}
