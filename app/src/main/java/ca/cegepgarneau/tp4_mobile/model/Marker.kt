package ca.cegepgarneau.tp4_mobile.model

import java.util.*

data class Marker(
    var markerid: Int,
    var isActive: Boolean,
    var firstname: String,
    var lastname: String,
    var picture: String,
    var latitude: Double,
    var longitude: Double,
    var message: String
    ): java.io.Serializable