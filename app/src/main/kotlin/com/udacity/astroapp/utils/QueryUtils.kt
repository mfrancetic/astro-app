package com.udacity.astroapp.utils

import android.util.Log
import androidx.core.net.toUri
import com.udacity.astroapp.models.Asteroid
import com.udacity.astroapp.models.EarthPhoto
import com.udacity.astroapp.models.Observatory
import com.udacity.astroapp.models.Photo
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class QueryUtils {

    companion object {
        private const val LOG_TAG = "QueryUtils"

        // API Base URLs
        private const val PHOTO_BASE_URL = "https://api.nasa.gov/planetary/apod?"
        private const val ASTEROID_BASE_URL = "https://api.nasa.gov/neo/rest/v1/feed?"
        private const val OBSERVATORY_BASE_URL =
            "https://maps.googleapis.com/maps/api/place/textsearch/json?query=planetarium&observatory"
        private const val OBSERVATORY_DETAILS_BASE_URL =
            "https://maps.googleapis.com/maps/api/place/details/json?"
        private const val EARTH_PHOTO_IMAGE_BASE_URL = "https://api.nasa.gov/EPIC/archive/natural/"
        private const val EARTH_PHOTO_BASE_URL = "https://api.nasa.gov/EPIC/api/natural/date/"

        // API Parameters
        private const val API_PARAM = "api_key"
        private const val DATE_PARAM = "date"
        private const val START_DATE_PARAM = "start_date"
        private const val END_DATE_PARAM = "end_date"
        private const val LOCATION_PARAM = "location"
        private const val RADIUS_PARAM = "radius"
        private const val GOOGLE_API_KEY_PARAM = "key"
        private const val PLACE_ID_PARAM = "placeid"
        private const val FIELDS_PARAM = "fields"
        private const val LANGUAGE_PARAM = "language"

        // Constants
        private const val RADIUS_KEY = "50000"
        private const val FIELDS_KEY = "name,photo,opening_hours,website,international_phone_number"

        // Timeouts
        private const val READ_TIME_OUT = 10000
        private const val CONNECT_TIME_OUT = 10000
        private const val SUCCESS_RESPONSE_CODE = 200
        private const val REQUEST_METHOD = "GET"
    }

    suspend fun fetchPhotoFromNetwork(date: String): Photo? =
        withContext(Dispatchers.IO) {
            try {
                val url = createPhotoUrl(date)
                val jsonResponse = makeHttpRequest(url)
                parsePhotoJson(jsonResponse, date)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error fetching photo for date: $date", e)
                null
            }
        }

    suspend fun fetchAsteroidsFromNetwork(
        startDate: String? = null,
        endDate: String? = null
    ): List<Asteroid> =
        withContext(Dispatchers.IO) {
            try {
                val url = createAsteroidUrl(startDate, endDate)
                val jsonResponse = makeHttpRequest(url)
                parseAsteroidsJson(jsonResponse)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error fetching asteroids", e)
                emptyList()
            }
        }

    suspend fun fetchEarthPhotosFromNetwork(date: String): List<EarthPhoto> =
        withContext(Dispatchers.IO) {
            try {
                val url = createEarthPhotoUrl(date)
                val jsonResponse = makeHttpRequest(url)
                parseEarthPhotosJson(jsonResponse, date)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error fetching Earth photos for date: $date", e)
                emptyList()
            }
        }

    suspend fun fetchObservatoriesFromNetwork(
        latitude: Double? = null,
        longitude: Double? = null
    ): List<Observatory> =
        withContext(Dispatchers.IO) {
            try {
                val location =
                    if (latitude != null && longitude != null) {
                        "$latitude,$longitude"
                    } else null

                val url = createObservatoryURL(location, "en")
                val jsonResponse = makeHttpRequest(url)
                parseObservatoriesJson(jsonResponse)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error fetching observatories", e)
                emptyList()
            }
        }

    suspend fun searchObservatoriesFromNetwork(query: String): List<Observatory> =
        withContext(Dispatchers.IO) {
            try {
                // For search, we would modify the base URL to include the search query
                val searchUrl =
                    "https://maps.googleapis.com/maps/api/place/textsearch/json?query=$query"
                val url = createSearchObservatoryURL(query, "en")
                val jsonResponse = makeHttpRequest(url)
                parseObservatoriesJson(jsonResponse)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error searching observatories with query: $query", e)
                emptyList()
            }
        }

    private fun createPhotoUrl(date: String): URL {
        val baseUri = PHOTO_BASE_URL.toUri()
        val uriBuilder =
            baseUri
                .buildUpon()
                .appendQueryParameter(API_PARAM, Secret.nasa_api_key)
                .appendQueryParameter(DATE_PARAM, date)
                .build()

        return URL(uriBuilder.toString())
    }

    private fun createAsteroidUrl(startDate: String?, endDate: String?): URL {
        val baseUri = ASTEROID_BASE_URL.toUri()
        val uriBuilder = baseUri.buildUpon()

        startDate?.let { uriBuilder.appendQueryParameter(START_DATE_PARAM, it) }
        endDate?.let { uriBuilder.appendQueryParameter(END_DATE_PARAM, it) }

        uriBuilder.appendQueryParameter(API_PARAM, Secret.nasa_api_key)

        return URL(uriBuilder.toString())
    }

    private fun createEarthPhotoUrl(date: String): URL {
        val baseUri = EARTH_PHOTO_BASE_URL.toUri()
        val uriBuilder =
            baseUri
                .buildUpon()
                .appendPath(date)
                .appendQueryParameter(API_PARAM, Secret.nasa_api_key)
                .build()

        return URL(uriBuilder.toString())
    }

    private fun createEarthPhotoImageUrl(date: String, image: String): URL {
        val baseUri = EARTH_PHOTO_IMAGE_BASE_URL.toUri()
        val formattedDate = date.replace("-", "/")

        val uriBuilder =
            baseUri
                .buildUpon()
                .appendEncodedPath("$formattedDate/")
                .appendEncodedPath("png/")
                .appendEncodedPath("$image.png")
                .appendQueryParameter(API_PARAM, Secret.nasa_api_key)
                .build()

        return URL(uriBuilder.toString())
    }

    private fun createObservatoryURL(latitudeLongitude: String?, language: String): URL {
        val baseUri = OBSERVATORY_BASE_URL.toUri()
        val builder =
            baseUri
                .buildUpon()
                .appendQueryParameter(GOOGLE_API_KEY_PARAM, Secret.google_play_services_api_key)
                .appendQueryParameter(FIELDS_PARAM, FIELDS_KEY)
                .appendQueryParameter(RADIUS_PARAM, RADIUS_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, language)

        latitudeLongitude?.let { builder.appendQueryParameter(LOCATION_PARAM, it) }

        return URL(builder.build().toString())
    }

    private fun createSearchObservatoryURL(query: String, language: String): URL {
        val searchUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?"
        val baseUri = searchUrl.toUri()
        val builder =
            baseUri
                .buildUpon()
                .appendQueryParameter("query", query)
                .appendQueryParameter(GOOGLE_API_KEY_PARAM, Secret.google_play_services_api_key)
                .appendQueryParameter(FIELDS_PARAM, FIELDS_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, language)

        return URL(builder.build().toString())
    }

    private fun createObservatoryDetailsUrl(placeId: String, language: String): URL {
        val baseUri = OBSERVATORY_DETAILS_BASE_URL.toUri()
        val builder =
            baseUri
                .buildUpon()
                .appendQueryParameter(GOOGLE_API_KEY_PARAM, Secret.google_play_services_api_key)
                .appendQueryParameter(PLACE_ID_PARAM, placeId)
                .appendQueryParameter(LANGUAGE_PARAM, language)

        return URL(builder.build().toString())
    }

    @Throws(IOException::class)
    private fun makeHttpRequest(url: URL): String {
        var jsonResponse = ""

        var urlConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null

        try {
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.readTimeout = READ_TIME_OUT
            urlConnection.connectTimeout = CONNECT_TIME_OUT
            urlConnection.requestMethod = REQUEST_METHOD
            urlConnection.connect()

            if (urlConnection.responseCode == SUCCESS_RESPONSE_CODE) {
                inputStream = urlConnection.inputStream
                jsonResponse = readFromStream(inputStream)
            } else {
                Log.e(LOG_TAG, "Error response code: ${urlConnection.responseCode}")
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Problem retrieving JSON results", e)
            throw e
        } finally {
            urlConnection?.disconnect()
            inputStream?.close()
        }

        return jsonResponse
    }

    @Throws(IOException::class)
    private fun readFromStream(inputStream: InputStream?): String {
        val output = StringBuilder()
        inputStream?.let { stream ->
            val inputStreamReader = InputStreamReader(stream, StandardCharsets.UTF_8)
            val reader = BufferedReader(inputStreamReader)

            var line = reader.readLine()
            while (line != null) {
                output.append(line)
                line = reader.readLine()
            }
            reader.close()
        }
        return output.toString()
    }

    private fun parsePhotoJson(jsonResponse: String, requestedDate: String): Photo? {
        if (jsonResponse.isEmpty()) return null

        return try {
            val jsonObject = JSONObject(jsonResponse)

            val title = jsonObject.optString("title")
            val description = jsonObject.optString("explanation")
            val date = jsonObject.optString("date", requestedDate)
            val url = jsonObject.optString("url")
            val hdUrl = jsonObject.optString("hdurl", url)
            val mediaType = jsonObject.optString("media_type", "image")

            Photo(
                photoTitle = title,
                photoDescription = description,
                photoDate = date,
                photoUrl = if (mediaType == "image") (url ?: hdUrl) else null,
                photoMediaType = mediaType,
                cacheTimestamp = System.currentTimeMillis()
            )
        } catch (e: JSONException) {
            Log.e(LOG_TAG, "Problem parsing photo JSON", e)
            null
        }
    }

    private fun parseAsteroidsJson(jsonResponse: String): List<Asteroid> {
        if (jsonResponse.isEmpty()) return emptyList()

        val asteroids = mutableListOf<Asteroid>()

        try {
            val jsonObject = JSONObject(jsonResponse)
            val nearEarthObjects = jsonObject.getJSONObject("near_earth_objects")

            val dates = nearEarthObjects.keys()
            while (dates.hasNext()) {
                val date = dates.next()
                val asteroidsArray = nearEarthObjects.getJSONArray(date)

                for (i in 0 until asteroidsArray.length()) {
                    val asteroidObject = asteroidsArray.getJSONObject(i)

                    val id = asteroidObject.getString("id")
                    val name = asteroidObject.getString("name")
                    val absoluteMagnitude = asteroidObject.getDouble("absolute_magnitude_h")

                    val estimatedDiameter = asteroidObject.getJSONObject("estimated_diameter")
                    val diameterKm = estimatedDiameter.getJSONObject("kilometers")
                    val minDiameter = diameterKm.getDouble("estimated_diameter_min")
                    val maxDiameter = diameterKm.getDouble("estimated_diameter_max")

                    val closeApproachData = asteroidObject.getJSONArray("close_approach_data")
                    if (closeApproachData.length() > 0) {
                        val approachData = closeApproachData.getJSONObject(0)
                        val approachDate = approachData.getString("close_approach_date")
                        val relativeVelocity = approachData.getJSONObject("relative_velocity")
                        val velocityKmh = relativeVelocity.getDouble("kilometers_per_hour")
                        val missDistance = approachData.getJSONObject("miss_distance")
                        val distanceKm = missDistance.getDouble("kilometers")

                        val isDangerous =
                            asteroidObject.getBoolean("is_potentially_hazardous_asteroid")

                        val asteroid =
                            Asteroid(
                                asteroidId = id.toIntOrNull() ?: 0,
                                asteroidName = name.replace("(", "").replace(")", ""),
                                asteroidDiameterMin = minDiameter,
                                asteroidDiameterMax = maxDiameter,
                                asteroidApproachDate = approachDate,
                                asteroidVelocity = "$velocityKmh km/h",
                                asteroidIsHazardous = isDangerous,
                                asteroidUrl = "https://ssd.jpl.nasa.gov/sbdb.cgi?sstr=${id}",
                                cacheTimestamp = System.currentTimeMillis()
                            )

                        asteroids.add(asteroid)
                    }
                }
            }
        } catch (e: JSONException) {
            Log.e(LOG_TAG, "Problem parsing asteroids JSON", e)
        }

        return asteroids
    }

    private fun parseEarthPhotosJson(
        jsonResponse: String,
        requestedDate: String
    ): List<EarthPhoto> {
        if (jsonResponse.isEmpty()) return emptyList()

        val earthPhotos = mutableListOf<EarthPhoto>()

        try {
            val jsonArray = JSONArray(jsonResponse)

            for (i in 0 until jsonArray.length()) {
                val photoObject = jsonArray.getJSONObject(i)

                val identifier = photoObject.getString("identifier")
                val caption = photoObject.getString("caption")
                val image = photoObject.getString("image")
                val date = photoObject.getString("date")

                // Create the full image URL
                val imageUrl = createEarthPhotoImageUrl(date, image).toString()

                val earthPhoto =
                    EarthPhoto(
                        earthPhotoCaption = caption,
                        earthPhotoUrl = imageUrl,
                        earthPhotoDateTime = date,
                        cacheTimestamp = System.currentTimeMillis()
                    )

                earthPhotos.add(earthPhoto)
            }
        } catch (e: JSONException) {
            Log.e(LOG_TAG, "Problem parsing Earth photos JSON", e)
        }

        return earthPhotos
    }

    private fun parseObservatoriesJson(jsonResponse: String): List<Observatory> {
        if (jsonResponse.isEmpty()) return emptyList()

        val observatories = mutableListOf<Observatory>()

        try {
            val jsonObject = JSONObject(jsonResponse)
            val results = jsonObject.getJSONArray("results")

            for (i in 0 until results.length()) {
                val observatoryObject = results.getJSONObject(i)

                val placeId = observatoryObject.getString("place_id")
                val name = observatoryObject.getString("name")
                val address = observatoryObject.getString("formatted_address")

                val geometry = observatoryObject.getJSONObject("geometry")
                val location = geometry.getJSONObject("location")
                val latitude = location.getDouble("lat")
                val longitude = location.getDouble("lng")

                var isOpen: Boolean? = null
                var openingHours: String? = null

                if (observatoryObject.has("opening_hours")) {
                    val openingHoursObject = observatoryObject.getJSONObject("opening_hours")
                    isOpen = openingHoursObject.optBoolean("open_now", false)
                }

                val observatory =
                    Observatory(
                        observatoryId = placeId,
                        observatoryName = name,
                        observatoryAddress = address,
                        observatoryPhoneNumber = null, // Would need details API call
                        observatoryOpenNow = isOpen ?: false,
                        observatoryOpeningHours = openingHours,
                        observatoryLatitude = latitude,
                        observatoryLongitude = longitude,
                        observatoryUrl = null, // Would need details API call
                        cacheTimestamp = System.currentTimeMillis()
                    )

                observatories.add(observatory)
            }
        } catch (e: JSONException) {
            Log.e(LOG_TAG, "Problem parsing observatories JSON", e)
        }

        return observatories
    }
}
