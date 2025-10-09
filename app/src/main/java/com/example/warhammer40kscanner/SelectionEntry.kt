package com.example.warhammer40kscanner


data class SelectionEntry(
    val id: Long,
    val name: String,
    val type: String,
    val idChapter: Long,
    var description: String? = null ,
    var imageUrl: String? = null,// nullable y mutable para poder actualizarla
    var statistics: StatisticsDTO? = null,
    var puntos: Int? = null
)