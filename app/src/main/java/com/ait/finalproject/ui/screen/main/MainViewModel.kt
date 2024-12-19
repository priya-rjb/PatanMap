package com.ait.finalproject.ui.screen.main

import android.net.Uri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ait.finalproject.data.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

sealed interface MainUiState {
    object Init : MainUiState
    object UploadPlaceInProgress : MainUiState
    object PlaceUploadSuccess : MainUiState
    data class PlacesRetrieved(val placeList: List<PlaceWithId>) : MainUiState
    data class Error(val error: String?) : MainUiState
}

class MainViewModel : ViewModel() {
    companion object {
        const val COLLECTION_PLACES = "places"
    }

    var mainUiState: MainUiState by mutableStateOf(MainUiState.Init)
    private var auth: FirebaseAuth
    var currentUserId: String
    var currentUserName: String

    init {
        auth = Firebase.auth
        currentUserId = auth.currentUser!!.uid
        currentUserName = auth.currentUser!!.email!!
    }

    fun uploadPlace(
        latLng: LatLng,
        placeTitle: String,
        placeText: String,
        categoryType: CategoryType,
        date: Date,
        imageUri: String
    ) {
        mainUiState = MainUiState.UploadPlaceInProgress

        val newPlace = Place(
            uid = currentUserId,
            author = currentUserName,
            placeTitle = placeTitle,
            placeText = placeText,
            lat = latLng.latitude,
            lng = latLng.longitude,
            categoryType = categoryType,
            date = date,
            imageUri = imageUri
        )

        val placeCollection = FirebaseFirestore.getInstance().collection(COLLECTION_PLACES)
        placeCollection.add(newPlace).addOnSuccessListener { documentReference ->
            mainUiState = MainUiState.PlaceUploadSuccess
        }.addOnFailureListener {
            mainUiState = MainUiState.Error(it.message)
        }
    }

    fun fetchPlacesByCategory(category: CategoryType, callback: (List<Place>) -> Unit) {
        FirebaseFirestore.getInstance().collection(COLLECTION_PLACES)
            .whereEqualTo("categoryType", category.name)
    }


        fun placeList() = callbackFlow {
            val snapshotListener =
                FirebaseFirestore.getInstance().collection(COLLECTION_PLACES)
                    .addSnapshotListener() { snapshot, e ->
                        val response = if (snapshot != null) {

                            val placeList = snapshot.toObjects(Place::class.java)
                            val placeWithIdList = mutableListOf<PlaceWithId>()

                            placeList.forEachIndexed { index, place ->
                                placeWithIdList.add(
                                    PlaceWithId(
                                        snapshot.documents[index].id,
                                        place
                                    )
                                )
                            }

                            MainUiState.PlacesRetrieved(
                                placeWithIdList
                            )
                        } else {
                            MainUiState.Error(e?.message.toString())
                        }

                        trySend(response) // emit this value through the flow
                    }
            awaitClose {
                snapshotListener.remove()
            }
        }

        fun deletePlace(placeKey: String) {
            FirebaseFirestore.getInstance().collection(
                COLLECTION_PLACES
            ).document(placeKey).delete()
        }
    }
