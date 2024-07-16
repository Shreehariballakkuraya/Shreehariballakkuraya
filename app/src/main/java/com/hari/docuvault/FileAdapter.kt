package com.hari.docuvault

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hari.docuvault.databinding.ItemFileBinding

class FileAdapter(private val fileList: List<String>) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileName = fileList[position]
        holder.bind(fileName)
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    inner class FileViewHolder(private val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(fileName: String) {
            // Display file name or URL
            binding.fileNameTextView.text = fileName // Format this if needed
        }
    }
}
