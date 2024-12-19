package com.ait.finalproject.data

import java.util.Date

data class Place(
    var uid: String = "",
    var author: String = "",
    var placeTitle: String = "",
    var placeText: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var likeType: LikeType = LikeType.NEUTRAL,
    var categoryType: CategoryType = CategoryType.ENTERTAINMENT,
    var date: Date = Date(),
    var atmosphere: Int = 0,
    var recommend: Boolean = false,
    var ranked: Float = 0f,
    var imageUri: String = ""
)
enum class LikeType {
    YES, NO, NEUTRAL;
}

data class PlaceWithId(
    val placeId: String,
    val place: Place
)
enum class CategoryType {
    MUSEUM, NATURE, SPORT, ENTERTAINMENT, RELIGION, FOOD, OTHER;
}