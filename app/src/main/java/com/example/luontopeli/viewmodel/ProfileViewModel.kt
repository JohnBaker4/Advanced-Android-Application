package com.example.luontopeli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // Lisää tämä!
class ProfileViewModel @Inject constructor() : ViewModel() { // Ja tämä!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Käyttäjän tila
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    // Tilastojen tila (tässä esimerkissä löytöjen määrä)
    private val _totalSpots = MutableStateFlow(0)
    val totalSpots: StateFlow<Int> = _totalSpots

    init {
        fetchStats()
    }

    private fun fetchStats() {
        val uid = auth.currentUser?.uid ?: return
        // Oletetaan että löydöt on tallennettu "nature_spots" -kokoelmaan uid:n mukaan
        db.collection("nature_spots")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, _ ->
                _totalSpots.value = snapshot?.size() ?: 0
            }
    }

    fun signInAnonymously() {
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _currentUser.value = auth.currentUser
                fetchStats()
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _totalSpots.value = 0
    }
}