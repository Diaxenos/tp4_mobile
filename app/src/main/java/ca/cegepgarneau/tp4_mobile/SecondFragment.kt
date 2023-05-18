package ca.cegepgarneau.tp4_mobile

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import ca.cegepgarneau.tp4_mobile.databinding.FragmentSecondBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.squareup.picasso.Picasso
import kotlin.concurrent.thread
import ca.cegepgarneau.tp4_mobile.model.Marker as Marker1


class SecondFragment : Fragment() , OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
    GoogleMap.InfoWindowAdapter, View.OnClickListener, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    val LOCATION_PERMISSION_CODE = 1
    val TAG = "TAG"
    lateinit var mMap: GoogleMap
    private lateinit var btAdd: Button
    private lateinit var messageText: EditText

    // Connexion à la base de données Firestore
    var db = FirebaseFirestore.getInstance()

    // Cr est un alias pour la collection "todos"
    var Cr = db.collection("markers")

    // Le listener qui permet de surveiller les changements dans la collection
    // est enregistré dans cette variable afin de pouvoir le désactiver dans le onStop()
    var registration: ListenerRegistration? = null


    // pour enregistrer la position de l'utilisateur

    var userLocation: Location? = null

    private var markerCamera: Marker? = null

    private lateinit var markerList: MutableList<Marker1>

    // pour suivre position de l'utilisateur
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Déclaration pour le callback de la mise à jour de la position de l'utilisateur
    // Le callback est appelé à chaque fois que la position de l'utilisateur change
    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            userLocation = locationResult.lastLocation
            Log.d(TAG, "onLocationResult: ${userLocation?.latitude} ${userLocation?.longitude}")
        }
    }

    private lateinit var locationRequest: LocationRequest

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!





    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        /// pour suivre la position de l'utilisateur
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        val root: View = binding.root
        markerList = java.util.ArrayList()



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        messageText = view.findViewById(R.id.messageText)
        btAdd = view.findViewById(R.id.bt_add)
        btAdd.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        // Initialisation de la carte
        mMap = googleMap
        // détection du click sur une fenêtre d'information d'un marqueur
        // (voir méthode onInfoWindowClick)
        mMap.setOnInfoWindowClickListener(this)
        // Permet de modifier l'apparence de la fenêtre d'information d'un marqueur
        // (voir méthode getInfoContents)
        mMap.setInfoWindowAdapter(this)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val share = sharedPref.all
        val nom = share.get("nom").toString()
        val prenom = share.get("prenom").toString()

        // Permet de détecter le click sur le bouton de position de l'utilisateur
        // (voir méthode onMyLocationButtonClick)
        mMap.setOnMyLocationButtonClickListener(this)
        // Permet de détecter le click sur la position de l'utilisateur
        // (voir méthode onMyLocationClick)
        mMap.setOnMyLocationClickListener(this)

        // Détecter click sur la carte
        mMap.setOnMapClickListener { latLng ->
            Log.d(TAG, "onMapClick: $latLng")
            // Ajoute un marqueur à l'endroit cliqué
            if (TextUtils.isEmpty(messageText.text)) {
                Toast.makeText(requireContext(), "Veuillez entrer un message", Toast.LENGTH_SHORT).show()
            }
            else {
                insertMarker(
                    latLng,
                    nom,
                    prenom,
                    messageText.text.toString(),
                    "https://robohash.org/$nom$prenom"
                )
                mMap.addMarker(
                    // Avoir le nom dans le shared preferences

                    // Ajout d'un marker à la position cliquée
                    MarkerOptions().position(latLng)
                        .title("$nom $prenom")
                        .draggable(false)
                        .snippet("Message")
                )
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))

                // Supprime marker ajouté par le bouton Add
                if (markerCamera != null) {
                    markerCamera!!.remove()
                }
            }

        }

        // Détecter un click long sur la carte
        mMap.setOnMapLongClickListener { latLng ->
            Log.d(TAG, "onMapClick: $latLng")
            if (TextUtils.isEmpty(messageText.text)) {
                Toast.makeText(requireContext(), "Veuillez entrer un message", Toast.LENGTH_SHORT).show()
            }
            else {
                insertMarker(latLng, nom, prenom, messageText.text.toString(),"https://robohash.org/$nom$prenom")
                mMap.addMarker(MarkerOptions().position(latLng).title("$nom $prenom").draggable(false))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
            }

        }

        // Écouteur pour le drag&drop d'un marqueur
        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                Log.d(TAG, "onMarkerDragStart: " + marker.position)
            }

            override fun onMarkerDrag(marker: Marker) {
                Log.d(TAG, "onMarkerDrag: " + marker.position)
            }

            override fun onMarkerDragEnd(marker: Marker) {
                Log.d(TAG, "onMarkerDragEnd: " + marker.position)
            }
        })

        // Active la localisation de l'utilisateur
        enableMyLocation()


        // Vérifie les permissions avant d'utiliser le service fusedLocationClient.getLastLocation()
        // qui permet de connaître la dernière position
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Demande la permission à l'utilisateur
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
            // Si la permission n'est pas accordée, on ne va pas plus loin
            return
        }


        // Récupère la dernière position connue
        fusedLocationClient.lastLocation
            .addOnSuccessListener(requireActivity()) { location ->
                // Vérifie que la position n'est pas null
                if (location != null) {
                    Log.d(TAG, "onSuccess: $location")
                    // Centre la carte sur la position de l'utilisateur au démarrage
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11f))
                }
            }

// Configuration pour mise à jour automatique de la position
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000L)
            .setMaxUpdateDelayMillis(5000L)
            .build()

// Création de la requête pour la mise à jour de la position
// avec la configuration précédente
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

// Création du client pour la mise à jour de la position.
// Le client va permettre de vérifier si la configuration est correcte,
// si l'utilisateur a activé ou désactivé la localisation
        val client = LocationServices.getSettingsClient(requireActivity())

// Vérifie que la configuration de la mise à jour de la position est correcte
// Si l'utilisateur a activé ou désactivé la localisation
        client.checkLocationSettings(request)
            .addOnSuccessListener {
                Log.d(TAG, "onSuccess: $it")
                // Si la configuration est correcte, on lance la mise à jour de la position
                fusedLocationClient.requestLocationUpdates(
                    // Configuration de la mise à jour de la position
                    locationRequest,
                    // Callback pour la mise à jour de la position
                    locationCallback,
                    null
                )
            }
            .addOnFailureListener {
                Log.d(TAG, "onFailure: $it")
                // Si la configuration n'est pas correcte, on affiche un message
                Toast.makeText(requireContext(), "Veuillez activer la localisation", Toast.LENGTH_SHORT).show()
            }


        // Parcours la liste de markers et positionne les marqueurs
        // On met l'objet message dans le Tag du marqueur
        // val galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        // galleryViewModel.getAllMarkers()

        // Parcours la liste de markers et positionne les marqueurs
        // On met l'objet message dans le Tag du marqueur
        val galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        Cr
            .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                Log.d("TAG", "onEvent: $value")
                markerList.clear()
                Log.d("VALUE", "onEvent: $value")
                if (value != null){
                    for (document in value!!) {
                        Log.d("DOCUMENT", "onEvent: $document")
                        val marker = document.toObject<ca.cegepgarneau.tp4_mobile.model.Marker>()
                        Log.d("MARKER", "onEvent: $marker")

                        markerList.add(marker)
                        Log.d("LISTE", "onEvent: $markerList")

                    }
                    for (marker2 in markerList) {
                        Log.d("MARKER2", "onEvent: $marker2")
                        val position = LatLng(marker2.latitude, marker2.longitude)
                        mMap.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(marker2.firstname + " " + marker2.lastname)
                                .snippet(marker2.message)
                        )?.tag = marker2
                    }
                }
            }
    }

    /**
     * Gestion des clics sur les fenêtres d'information des marqueurs
     */
    override fun onInfoWindowClick(marker: Marker) {

        val location = Location("Marker")
        location.latitude = marker.position.latitude
        location.longitude = marker.position.longitude

        // calculer la distance de l'utilisateur avec le marqueur sélectionné
        val distance = userLocation?.distanceTo(location)
        if (distance != null) {
            Toast.makeText(requireContext(), "Distance: ${distance / 1000}km", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Méthode pour modifier le contenu d'une fenêtre d'information
     * en utilisant un layout
     */
    override fun getInfoContents(marker: Marker): View? {
        val view: View = LayoutInflater.from(requireContext()).inflate(R.layout.marker_layout, null)
        val tvAuthor = view.findViewById<TextView>(R.id.tv_author)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val iv = view.findViewById<ImageView>(R.id.imageView)
        val latitude = marker.position.latitude
        tvAuthor.text = marker.title
        // On récupère le message qui est dans le Tag du marqueur
        val marker2: Marker1? = marker.tag as Marker1?
        tvMessage.text = if (marker2 != null) marker2.message else null
        if (marker2 != null) {
            Picasso.get().load(marker2.picture).into(iv)
        }
        return view
    }

    /**
     * Méthode pour modifier l'apparence d'une fenêtre d'information
     */
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    /**
     * Gestion des clics sur les boutons
     */
    override fun onClick(view: View) {
        Log.d("NOUVEAU MARQUEUR", TextUtils.isEmpty(messageText.text).toString())
        if (TextUtils.isEmpty(messageText.text)) {
            Toast.makeText(requireContext(), "Veuillez entrer un message", Toast.LENGTH_SHORT).show()
        }
        else{
            when (view.id) {
                R.id.bt_add -> {
                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val share = sharedPref.all
                    val nom = share.get("nom").toString()
                    val prenom = share.get("prenom").toString()

                    // positionner un marqueur au centre de la carte
                    val cameraPosition = mMap.cameraPosition
                    val position =
                        LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude)
                    markerCamera = mMap.addMarker(MarkerOptions().position(position).title("Titre"))
                    insertMarker(
                        position,
                        nom,
                        prenom,
                        messageText.text.toString(),
                        "https://robohash.org/$nom$prenom"
                    )
                }

            }
        }
    }
    fun insertMarker(Latlng: LatLng, firstname: String, lastname: String, message: String, picture: String) {
        Log.d("NOUVEAU MARQUEUR", TextUtils.isEmpty(messageText.text).toString())
        if (TextUtils.isEmpty(messageText.text)) {
            Toast.makeText(requireContext(), "Veuillez entrer un message", Toast.LENGTH_SHORT).show()
        }
        else {
            val galleryViewModel2 = ViewModelProvider(this).get(GalleryViewModel::class.java)
            val marker = Marker1(
                0,
                false,
                firstname,
                lastname,
                picture,
                Latlng.latitude,
                Latlng.longitude,
                message
            )
            thread {
                galleryViewModel2.insert(marker)
            }
        }
        messageText.text.clear()
    }

    /**
     * Méthode pour détecter le click sur le bouton de position
     */
    override fun onMyLocationButtonClick(): Boolean {
        Log.d(TAG, "onMyLocationButtonClick: ")
        return false
    }

    /**
     * Méthode pour détecter le click sur la position de l'utilisateur
     */
    override fun onMyLocationClick(location: Location) {
        Log.d(TAG, "onMyLocationClick: $location")
    }

    /**
     * Permet d'activer la localisation de l'utilisateur
     */
    private fun enableMyLocation() {
        // vérification si la permission de localisation est déjà donnée
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (mMap != null) {
                // Permet d'afficher le bouton pour centrer la carte sur la position de l'utilisateur
                mMap.isMyLocationEnabled = true
            }
        } else {
            // La permission est manquante, demande donc la permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

}