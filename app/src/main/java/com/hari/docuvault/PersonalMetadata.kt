package com.hari.docuvault

data class PersonalMetadata(
    val fileName: String? = null,
    val fileUrl: String? = null,
    val documentType: String? = null,
    val expiryDate: String? = null
) {
    override fun toString(): String {
        return "Name: $fileName, Type: $documentType, Expiry: $expiryDate"
    }
}
