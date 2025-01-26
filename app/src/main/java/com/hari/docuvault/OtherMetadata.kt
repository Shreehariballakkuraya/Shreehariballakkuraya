package com.hari.docuvault

data class OtherMetadata(
    val documentTitle: String? = null,
    val documentType: String? = null,
    val additionalInfo: String? = null,
    val category: String? = null,
    val tags: String? = null,
    val dateAdded: String? = null,
    val fileUrl: String? = null
) {
    var fileName: String? = null
}
