package com.hari.docuvault

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class VehicleMetadataAdapter(
    private val context: Context,
    private val metadataList: List<VehicleMetadata>
) : RecyclerView.Adapter<VehicleMetadataAdapter.MetadataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetadataViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle_metadata, parent, false)
        return MetadataViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MetadataViewHolder, position: Int) {
        val metadata = metadataList[position]
        holder.bind(metadata)
        holder.itemView.setOnClickListener {
            val uri = Uri.parse(metadata.fileUrl)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                type = if (metadata.fileName.endsWith(".pdf")) "application/pdf" else "image/*"
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No application found to open this file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = metadataList.size

    class MetadataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val vehicleNameTextView: TextView = itemView.findViewById(R.id.vehicleNameTextView)
        private val documentTypeTextView: TextView = itemView.findViewById(R.id.documentTypeTextView)
        private val expiryDateTextView: TextView = itemView.findViewById(R.id.expiryDateTextView)
        private val vehicleNumberTextView: TextView = itemView.findViewById(R.id.vehicleNumberTextView)
        private val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)

        fun bind(metadata: VehicleMetadata) {
            vehicleNameTextView.text = metadata.vehicleName
            documentTypeTextView.text = metadata.documentType
            expiryDateTextView.text = metadata.expiryDate
            vehicleNumberTextView.text = metadata.vehicleNumber
            fileNameTextView.text = metadata.fileName
        }
    }
}
