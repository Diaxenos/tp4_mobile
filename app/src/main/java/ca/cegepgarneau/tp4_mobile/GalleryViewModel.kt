package ca.cegepgarneau.tp4_mobile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ca.cegepgarneau.tp4_mobile.model.Marker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    // Connexion à la base de données Firestore
    var db = FirebaseFirestore.getInstance()

    var Cr = db.collection("markers")

    var registration: ListenerRegistration? = null

    private val markers: MutableList<Marker> = mutableListOf()
    fun getAllMarkers(): MutableList<Marker>{
        Cr
            .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                Log.d("TAG", "onEvent: $value")
                markers.clear()
                Log.d("VALUE", "onEvent: $value")
                if (value != null){
                    for (document in value!!) {
                        Log.d("DOCUMENT", "onEvent: $document")
                        val marker = document.toObject<Marker>()
                        Log.d("MARKER", "onEvent: $marker")

                        markers.add(marker)
                        Log.d("LISTE", "onEvent: $markers")

                    }
                }
            }
        Log.d("LISTE", "onEvent: $markers")
        return markers
    }

    fun insert(marker: Marker) {
        Cr.add(marker)
        Log.d("MARKER AJOUTER", "insert: $marker")
    }
}