package edu.sliit.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.newdeveopmen.HistoryFragment
import com.google.android.material.snackbar.Snackbar
import edu.sliit.myapplication.databinding.ActivityMainBinding
import edu.sliit.myapplication.services.SyncService
import edu.sliit.myapplication.utils.NetworkMonitor
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var syncService: SyncService
    private var isOnline = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkMonitor = NetworkMonitor(this)
        syncService = SyncService(this)

        // Check initial network status
        isOnline = networkMonitor.isOnline()
        if (!isOnline) {
            showOfflineIndicator()
        }

        // Monitor network connectivity
        observeNetworkChanges()

        // Set home as default
        loadFragment(HomeFragment())
        binding.bottomNavigation.selectedItemId = R.id.nav_home

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_scan -> {
                    loadFragment(ScanFragment())
                    true
                }
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun observeNetworkChanges() {
        lifecycleScope.launch {
            networkMonitor.observeNetworkConnectivity().collect { online ->
                val wasOffline = !isOnline
                isOnline = online

                if (online) {
                    if (wasOffline) {
                        // Just came online
                        showOnlineIndicator()
                        // Auto-sync when coming back online
                        performAutoSync()
                    }
                } else {
                    showOfflineIndicator()
                }
            }
        }
    }

    private fun showOfflineIndicator() {
        val pendingCount = syncService.getPendingSyncCount()
        val message = if (pendingCount > 0) {
            "📡 Offline Mode ($pendingCount pending syncs)"
        } else {
            "📡 Offline Mode"
        }
        
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(android.R.color.holo_orange_dark))
            .show()
    }

    private fun showOnlineIndicator() {
        Snackbar.make(binding.root, "🌐 Back Online", Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(android.R.color.holo_green_dark))
            .show()
    }

    private fun performAutoSync() {
        if (!syncService.isSyncNeeded()) {
            android.util.Log.d("MainActivity", "No pending syncs")
            return
        }

        val pendingCount = syncService.getPendingSyncCount()
        Toast.makeText(this, "🔄 Syncing $pendingCount items...", Toast.LENGTH_SHORT).show()

        syncService.syncAll { success, synced, failed ->
            val message = if (success) {
                "✅ Synced $synced items"
            } else {
                "⚠️ Synced $synced, failed $failed"
            }
            
            runOnUiThread {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(
                        if (success) getColor(android.R.color.holo_green_dark)
                        else getColor(android.R.color.holo_orange_dark)
                    )
                    .show()
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}