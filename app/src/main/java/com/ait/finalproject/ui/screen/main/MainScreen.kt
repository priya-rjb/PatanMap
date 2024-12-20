package com.ait.finalproject.ui.screen.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.TempleHindu
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbsUpDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ait.finalproject.R
import com.ait.finalproject.data.CategoryType
import com.ait.finalproject.data.LikeType
import com.ait.finalproject.data.Place
import com.ait.finalproject.ui.navigation.InnerNavigation
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableStateOf(0) }
    var innerNavController: NavHostController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Patan") },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                actions = {
                    IconButton(onClick = {
                        //
                    }) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(content = {
                NavigationBar(
                    //containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    NavigationBarItem(selected = selectedBottomTab == 0,
                        onClick = {
                            selectedBottomTab = 0
                            innerNavController.navigate(InnerNavigation.MapView.route) {
                                innerNavController.popBackStack()
                            }
                        },
                        label = {
                            Text(
                                text = "Map",
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map",
                            )
                        })
                    NavigationBarItem(selected = selectedBottomTab == 1,
                        onClick = {
                            selectedBottomTab = 1
                            innerNavController.navigate(InnerNavigation.ListView.route) {
                                innerNavController.popBackStack()
                            }
                        },
                        label = {
                            Text(
                                text = "List",
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "List",
                            )
                        })
                }
            })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            NavHost(
                navController = innerNavController,
                startDestination = InnerNavigation.MapView.route
            ) {
                composable(InnerNavigation.MapView.route) {
                    MapView(mainViewModel)
                }
                composable(InnerNavigation.ListView.route) {
                    ListView(mainViewModel)
                }
            }
            when (mainViewModel.mainUiState) {
                is MainUiState.Error -> {
                    Text(text = (mainViewModel.mainUiState as MainUiState.Error).error!!)
                }

                MainUiState.Init -> {}
                MainUiState.PlaceUploadSuccess -> {}
                MainUiState.UploadPlaceInProgress -> {
                    CircularProgressIndicator()
                }

                is MainUiState.PlacesRetrieved -> {}
            }
        }
    }
}

@Composable
fun MapView(
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current

    var showAdd by remember { mutableStateOf(false) }
    var clickedCoord by remember { mutableStateOf(LatLng(27.6588, 85.3247)) }
    var previousPlaces by remember { mutableStateOf<List<Place>>(emptyList()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(27.6588, 85.3247), 12f)
    }

    var uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                zoomGesturesEnabled = true
            )
        )
    }
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL
            )
        )
    }

    val startPoint = LatLng(27.6588, 85.3247)
    val endPoint = LatLng(27.6694, 85.3145)

// Generate a smooth curve with 100 intermediate points and a small curvature value
    //pulchowk to durbar square:
    val polylineCoordinates1 = calculateCurvePoints(LatLng(27.6782, 85.3169), LatLng(27.67502, 85.326972), numPoints = 100, curvature = 0.002)
    //durbar square to lagankhel
    val polylineCoordinates2 = calculateCurvePoints(LatLng(27.67502, 85.326972), LatLng(27.6660, 85.3227), numPoints = 100, curvature = 0.002)
    //lagankhel to jawlakhel:
    val polylineCoordinates3 = calculateCurvePoints(LatLng(27.6660, 85.3227),LatLng(27.6744, 85.3123), numPoints = 100, curvature = 0.002)



    //Durbar Square -- Bhimsen -- banglamukhi:
    //durbar square to lagankhel:
    val polylineCoordinates4 = calculateCurvePoints(LatLng(27.67502, 85.326972), LatLng(27.675211, 85.324798), numPoints = 100, curvature = 0.002)
    //lagankhel to jawalakhel:
    val polylineCoordinates5 = calculateCurvePoints(LatLng(27.675211, 85.324798),LatLng(27.67652733, 85.3258448), numPoints = 100, curvature = 0.002)




//    // Example coordinates for the polyline
//    val polylineCoordinates = listOf(
//        LatLng(27.67165, 85.31651), //labim mall
//        LatLng(27.6660, 85.3227), //lagankhel
//        LatLng(27.6744, 85.3123), //jawlakhel
//    )

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        uiSettings = uiSettings,
        properties = mapProperties,
        onMapClick = {
            showAdd = true
            clickedCoord = it
        },
        cameraPositionState = cameraPositionState
    ) {
        val placeListState = mainViewModel.placeList().collectAsState(MainUiState.Init)

        if (placeListState.value is MainUiState.PlacesRetrieved) {
            for (place in (placeListState.value as MainUiState.PlacesRetrieved).placeList) {
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            place.place.lat,
                            place.place.lng
                        )
                    ),
                    title = "${place.place.placeTitle}"
                )
            }
        }

        // Add the polyline to the map
        Polyline(
            points = polylineCoordinates1,
            color = Color.Magenta, // Change color as needed
            width = 10f // Adjust the width of the polyline
        )
        Polyline(
            points = polylineCoordinates2,
            color = Color.Magenta, // Change color as needed
            width = 10f // Adjust the width of the polyline
        )
        Polyline(
            points = polylineCoordinates3,
            color = Color.Magenta, // Change color as needed
            width = 10f // Adjust the width of the polyline
        )

        Polyline(
            points = polylineCoordinates4,
            color = Color.Blue, // Change color as needed
            width = 10f // Adjust the width of the polyline
        )
        Polyline(
            points = polylineCoordinates5,
            color = Color.Blue, // Change color as needed
            width = 10f // Adjust the width of the polyline
        )
    }

    if (showAdd) {
        mainViewModel.fetchPlacesByCategory(CategoryType.ENTERTAINMENT) { places ->
            previousPlaces = places
        }
        AddNewPlaceDialog(
            latLng = clickedCoord,
            onAddPlace = { placeTitle, placeText, categorySelected, date, imageUri ->
                mainViewModel.uploadPlace(clickedCoord, placeTitle, placeText, categorySelected, date, imageUri)
                showAdd = false
            },
            onDialogClose = { showAdd = false }
        )
    }
}


//@Composable
//fun MapView(
//    mainViewModel: MainViewModel
//) {
//    val context = LocalContext.current
//
//    var showAdd by remember { mutableStateOf(false) }
//    var clickedCoord by remember { mutableStateOf(LatLng(27.6588, 85.3247)) }
//    var previousPlaces by remember { mutableStateOf<List<Place>>(emptyList()) }
//
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(LatLng(27.6588, 85.3247), 12f)
//    }
//
//    var uiSettings by remember {
//        mutableStateOf(
//            MapUiSettings(
//                zoomControlsEnabled = true,
//                zoomGesturesEnabled = true
//            )
//        )
//    }
//    var mapProperties by remember {
//        mutableStateOf(
//            MapProperties(
//                mapType = MapType.NORMAL
//            )
//        )
//    }
//
//    GoogleMap(
//        modifier = Modifier.fillMaxSize(),
//        uiSettings = uiSettings,
//        properties = mapProperties,
//        onMapClick = {
//            showAdd = true
//            clickedCoord = it
//        },
//        cameraPositionState = cameraPositionState
//    )
//    {
//        val placeListState = mainViewModel.placeList().collectAsState(MainUiState.Init)
//
//        if (placeListState.value is MainUiState.PlacesRetrieved) {
//            for (place in (placeListState.value as MainUiState.PlacesRetrieved).placeList) {
//                Marker(
//                    state = MarkerState(
//                        position = LatLng(
//                            place.place.lat,
//                            place.place.lng
//                        )
//                    ),
//                    title = "${place.place.placeTitle}"
//                )
//            }
//        }
//    }
//
//
//    if (showAdd) {
//        mainViewModel.fetchPlacesByCategory(CategoryType.ENTERTAINMENT) { places ->
//            previousPlaces = places
//        }
//            AddNewPlaceDialog(
//                latLng = clickedCoord,
//                onAddPlace = { placeTitle, placeText, categorySelected, date, imageUri ->
//                    mainViewModel.uploadPlace(clickedCoord, placeTitle, placeText, categorySelected, date, imageUri)
//                    showAdd = false
//                },
//                onDialogClose = { showAdd = false }
//            )
//        //}
//    }
//}


@Composable
fun ListView(mainViewModel: MainViewModel) {
    val placeListState = mainViewModel.placeList().collectAsState(MainUiState.Init)

    if (placeListState.value == MainUiState.Init) {
        Text(text = "Initializing..")
    } else if (placeListState.value is MainUiState.PlacesRetrieved) {
        LazyColumn() {
            items((placeListState.value as MainUiState.PlacesRetrieved).placeList) {
                PlaceCard(
                    place = it.place,
                    onRemoveItem = {
                        mainViewModel.deletePlace(it.placeId)
                    },
                    currentUserId = mainViewModel.currentUserId
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceCard(
    place: Place,
    onRemoveItem: () -> Unit = {},
    currentUserId: String = ""
) {

    val categoryIcon: ImageVector = when (place.categoryType) {
        CategoryType.ENTERTAINMENT -> Icons.Filled.Movie
        CategoryType.NATURE -> Icons.Filled.Nature
        CategoryType.SPORT -> Icons.Filled.Sports
        CategoryType.FOOD -> Icons.Filled.Movie
        CategoryType.RELIGION -> Icons.Filled.TempleHindu
        CategoryType.MUSEUM-> Icons.Filled.Museum
        CategoryType.OTHER -> Icons.Filled.Place
    }
    var expanded by remember { mutableStateOf(false) } // initial value

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),
        modifier = Modifier.padding(5.dp)
    ) {

        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = place.author)
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = LocalContext.current
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(text = place.placeTitle)
//                    Text(text = place.placeText)
                    Text(text = "${place.lat}, ${place.lng}")
//                    Text(text = "${place.ranked}")
                }
                Spacer(modifier = Modifier.fillMaxSize(0.10f))
                Icon(
                    imageVector = Icons.Filled.Directions,
                    contentDescription = "Map",
                    modifier = Modifier.clickable {
                        intentStreetMaps(context, place)
                    },
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.fillMaxSize(0.05f))

                if (currentUserId == place.uid) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier
                            .clickable { onRemoveItem() }
                            .padding(start = 8.dp),
                        tint = Color.Red
                    )
                }

                //ADDING THE EXPANDING FEATURE
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        //ACCESSIBLE!!!!
                        contentDescription = if (expanded) {
                            "Less"
                        } else {
                            "More"
                        }
                    )
                }
            }
            //add image:

            //IF IT IS EXPANDED THAT SHOW MORE INFORMATION
            if (expanded) {
                Image(
                    painter = painterResource(id = R.drawable.pic),
                    contentDescription = "",
                    contentScale = ContentScale.Fit
                )
                Text(text = place.placeText)

            }
        }
    }
}
private fun intentStreetMaps(context: Context, place: Place) {
    val gmmIntentUri = Uri.parse(
        //"google.streetview:cbll=29.9774614,31.1329645&cbp=0,30,0,0,-15")
        "google.streetview:cbll=${place.lat},${place.lng}&cbp=0,30,0,0,-15")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    context.startActivity(mapIntent)
}

// Function to calculate intermediate points for a curve
fun calculateCurvePoints(start: LatLng, end: LatLng, numPoints: Int, curvature: Double): List<LatLng> {
    val curvePoints = mutableListOf<LatLng>()

    for (i in 0..numPoints) {
        val t = i.toDouble() / numPoints
        val lat = (1 - t) * start.latitude + t * end.latitude
        val lng = (1 - t) * start.longitude + t * end.longitude + curvature * Math.sin(Math.PI * t)
        curvePoints.add(LatLng(lat, lng))
    }

    return curvePoints
}