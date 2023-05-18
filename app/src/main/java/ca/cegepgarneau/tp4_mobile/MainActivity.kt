package ca.cegepgarneau.tp4_mobile

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.content.BroadcastReceiver
import android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED
import android.content.Intent.ACTION_SCREEN_ON
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import ca.cegepgarneau.tp4_mobile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // br est une instance de la classe MyBroadCastReceiver
    val br: BroadcastReceiver = MyBroadCastReceiver()

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    // NotificationManagerCompat est une classe utilitaire qui permet d'interagir avec le système de notification.
    private val notificationManager: NotificationManagerCompat? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Ajout de préférences par défaut pour les préférences de l'application (SettingsActivity)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPref.edit()
        editor.putString("nom", "Cegep")
        editor.putString("prenom", "Garneau")
        editor.apply()

        // Demande de permission pour les notifications
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            // Lancement de l'activité SettingsActivity
            R.id.mn_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendOnChannel(v: View?) {

        var builder = NotificationCompat.Builder(this, CHANNEL_2_ID)
            .setSmallIcon(R.drawable.ic_android)
            .setContentTitle("Marker")
            .setContentText("Le marker vient d'être ajouté")

        with(NotificationManagerCompat.from(this)) {
            notify(2, builder.build())
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Navigation vers le fragment précédent
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        // filter permet de filtrer les intents reçus par le BroadcastReceiver
        val filter = IntentFilter(ACTION_SCREEN_ON)
        // registerReceiver() permet d'enregistrer le BroadcastReceiver
        registerReceiver(br, filter)

        val filter2 = IntentFilter(ACTION_AIRPLANE_MODE_CHANGED)
        registerReceiver(br, filter2)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(br)
    }

    companion object {
        const val CHANNEL_2_ID = "channel2"
    }
}