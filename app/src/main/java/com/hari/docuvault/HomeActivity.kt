package com.hari.docuvault

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.docuvault.R

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage) // Make sure this layout exists
    }
}
