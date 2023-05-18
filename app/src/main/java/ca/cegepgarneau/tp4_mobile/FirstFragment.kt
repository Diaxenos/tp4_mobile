package ca.cegepgarneau.tp4_mobile

import android.content.ContentValues.TAG
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ca.cegepgarneau.tp4_mobile.R
import androidx.navigation.fragment.findNavController
import ca.cegepgarneau.tp4_mobile.databinding.FragmentFirstBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(){

    private var _binding: FragmentFirstBinding? = null

    private lateinit var auth: FirebaseAuth

    private var connected = false

    private lateinit var tvEmail : EditText
    private lateinit var tvPassword : EditText

    private lateinit var mediaPlayer: MediaPlayer

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        val db = Firebase.firestore

        auth = FirebaseAuth.getInstance()


        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvEmail = view.findViewById(R.id.emailTxt)
        tvPassword = view.findViewById(R.id.editTextTextPassword)

        binding.buttonFirst.setOnClickListener {
            if(tvEmail.text.toString() != "" && tvPassword.text.toString() != ""){
                signInWithEmailAndPassword(tvEmail.text.toString(), tvPassword.text.toString())
                if (connected){
                    // Gérer l'authentification ici
                    mediaPlayer = MediaPlayer.create(activity, R.raw.sons)
                    mediaPlayer.start()
                    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)}
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // L'authentification est réussie
                    val user = auth.currentUser
                    connected = true
                    // Faire quelque chose avec l'utilisateur connecté
                    Toast.makeText(
                        requireContext(),
                        "Authentification réussie",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Une erreur s'est produite lors de l'authentification
                    connected = false
                    Toast.makeText(
                        requireContext(),
                        "Erreur d'authentification: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}