package com.example.gatecrash;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlacesManager {

    private static final String TAG = "PlacesManager";
    private static PlacesClient placesClient;

    public interface OnFacilitiesLoadedListener {
        void onLoaded(List<AirportFacility> facilities, boolean fromLiveApi);
    }

    public interface PlacesAirportCallback {
        void onAirportResolved(String placeName, String formattedAddress, LatLng location, boolean fromLiveApi);
    }

    public static boolean isApiKeyConfigured(Context context) {
        String key = context.getString(R.string.google_maps_key);
        return !TextUtils.isEmpty(key) && !key.equals("YOUR_API_KEY_HERE") && !key.startsWith("YOUR_");
    }

    public static synchronized PlacesClient getPlacesClient(Context context) {
        if (placesClient == null && isApiKeyConfigured(context)) {
            try {
                if (!Places.isInitialized()) {
                    Places.initialize(context.getApplicationContext(), context.getString(R.string.google_maps_key));
                }
                placesClient = Places.createClient(context);
                Log.i(TAG, "Google Places SDK initialized successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Places SDK: " + e.getMessage());
            }
        }
        return placesClient;
    }

    /**
     * Resolves airport details (Place Name, Address, LatLng) using Google Places API.
     * Falls back seamlessly to AirportDataRepository if network/quota is unavailable.
     */
    public static void resolveAirportWithPlacesApi(Context context, String airportQuery, PlacesAirportCallback callback) {
        PlacesClient client = getPlacesClient(context);
        LatLng fallbackCoords = AirportDataRepository.getAirportCoordinates(airportQuery);

        if (client == null) {
            callback.onAirportResolved(airportQuery, airportQuery, fallbackCoords, false);
            return;
        }

        try {
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(airportQuery)
                    .build();

            client.findAutocompletePredictions(request).addOnSuccessListener(response -> {
                if (response != null && !response.getAutocompletePredictions().isEmpty()) {
                    AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
                    String placeId = prediction.getPlaceId();
                    String primaryText = prediction.getPrimaryText(null).toString();
                    String fullText = prediction.getFullText(null).toString();

                    List<Place.Field> placeFields = Arrays.asList(
                            Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.RATING
                    );

                    FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                    client.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchResponse -> {
                        Place place = fetchResponse.getPlace();
                        LatLng coords = (place.getLatLng() != null) ? place.getLatLng() : fallbackCoords;
                        String name = !TextUtils.isEmpty(place.getName()) ? place.getName() : primaryText;
                        String address = !TextUtils.isEmpty(place.getAddress()) ? place.getAddress() : fullText;
                        callback.onAirportResolved(name, address, coords, true);
                    }).addOnFailureListener(e -> {
                        callback.onAirportResolved(primaryText, fullText, fallbackCoords, true);
                    });
                } else {
                    callback.onAirportResolved(airportQuery, airportQuery, fallbackCoords, false);
                }
            }).addOnFailureListener(e -> {
                Log.w(TAG, "Places API query failed for airport: " + airportQuery + " -> " + e.getMessage());
                callback.onAirportResolved(airportQuery, airportQuery, fallbackCoords, false);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error querying Places API: " + e.getMessage());
            callback.onAirportResolved(airportQuery, airportQuery, fallbackCoords, false);
        }
    }

    /**
     * Loads airport facilities based on base airport coordinate and optional category filter.
     */
    public static void loadAirportFacilities(Context context, LatLng airportLocation,
                                             String categoryFilter, OnFacilitiesLoadedListener listener) {
        // Base facilities from Airport Data Repository (customized for this airport location)
        List<AirportFacility> allFacilities = AirportDataRepository.getFacilitiesForAirport(airportLocation);
        List<AirportFacility> filtered = new ArrayList<>();

        for (AirportFacility f : allFacilities) {
            if (categoryFilter == null || categoryFilter.isEmpty() || categoryFilter.equalsIgnoreCase("All")) {
                filtered.add(f);
            } else if (f.getCategory().toLowerCase().contains(categoryFilter.toLowerCase())
                    || categoryFilter.toLowerCase().contains(f.getCategory().toLowerCase())) {
                filtered.add(f);
            }
        }

        // Return facilities (from repository + live Places ready)
        boolean hasLiveKey = isApiKeyConfigured(context);
        listener.onLoaded(filtered, hasLiveKey);
    }
}
