package ca.cegepgarneau.tp4_mobile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ca.cegepgarneau.tp4_mobile.model.Marker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    // Connexion à la base de données Firestore
    var db = FirebaseFirestore.getInstance()

    var Cr = db.collection("markers")

    var registration: ListenerRegistration? = null

    fun getAllMarkers(): LiveData<List<Marker>> {
        // Tous les markers dans firebase
        Cr.get()
        return getAllMarkers()
    }
    fun insert(marker: Marker) {
        Cr
            .add(marker)
            .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot added with ID: ") }
            .addOnFailureListener { e -> Log.w("TAG", "Error adding document", e) }
    }
    fun insertAll(marker: List<Marker>) {

    }
    fun update(marker: Marker) {
    }
    fun deleteAll() {
    }
    fun getMarkerById(id: Int): LiveData<Marker> {
        return getMarkerById(id)
    }
}