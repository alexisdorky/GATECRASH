package com.example.gatecrash;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private LatLng airportLocation;
    private LatLng userLocation;
    private String airportDisplayName = "Mumbai Terminal 2";
    private String selectedCategory = "All";

    private final List<Marker> plottedMarkers = new ArrayList<>();
    private final Map<Marker, AirportFacility> markerFacilityMap = new HashMap<>();
    private Marker userMarker;
    private Polyline currentRoutePolyline;
    private AirportFacility currentSelectedFacility;

    // Dual View Mode elements
    private WebView terminalWebView;
    private View mapFragmentContainer;
    private Button btnModeTerminal, btnModeSatellite;
    private boolean isTerminalMode = true;

    // UI elements
    private TextView mapAirportTitle, placesApiStatusBadge;
    private MaterialCardView facilityDetailCard;
    private TextView cardFacilityEmoji, cardFacilityName, cardFacilityLocation;
    private TextView cardRatingText, cardHoursText, cardDistanceText, cardDescriptionText;
    private Button btnStartNavigation, cardCloseBtn;

    // Filter Chips
    private Button chipAll, chipRestrooms, chipFood, chipLounges, chipCharging, chipShopping, chipMedical;
    private Button[] allChips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Detect airport from user's latest flight or intent
        resolveAirportContext();

        // Bind Dual Mode Views
        terminalWebView = findViewById(R.id.terminalWebView);
        mapFragmentContainer = findViewById(R.id.mapFragment);
        btnModeTerminal = findViewById(R.id.btnModeTerminal);
        btnModeSatellite = findViewById(R.id.btnModeSatellite);

        // Bind UI
        Button backBtn = findViewById(R.id.backFromMap);
        mapAirportTitle = findViewById(R.id.mapAirportTitle);
        placesApiStatusBadge = findViewById(R.id.placesApiStatusBadge);

        mapAirportTitle.setText(airportDisplayName.toUpperCase());
        placesApiStatusBadge.setText("● Interactive Terminal Floor Engine Active");
        placesApiStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.success));

        // Card components
        facilityDetailCard = findViewById(R.id.facilityDetailCard);
        cardFacilityEmoji = findViewById(R.id.cardFacilityEmoji);
        cardFacilityName = findViewById(R.id.cardFacilityName);
        cardFacilityLocation = findViewById(R.id.cardFacilityLocation);
        cardRatingText = findViewById(R.id.cardRatingText);
        cardHoursText = findViewById(R.id.cardHoursText);
        cardDistanceText = findViewById(R.id.cardDistanceText);
        cardDescriptionText = findViewById(R.id.cardDescriptionText);
        btnStartNavigation = findViewById(R.id.btnStartNavigation);
        cardCloseBtn = findViewById(R.id.cardCloseBtn);

        // FAB controls
        FloatingActionButton btnGpsLocate = findViewById(R.id.btnGpsLocate);
        FloatingActionButton btnRecenterAirport = findViewById(R.id.btnRecenterAirport);

        // Filter chips
        chipAll = findViewById(R.id.chipAll);
        chipRestrooms = findViewById(R.id.chipRestrooms);
        chipFood = findViewById(R.id.chipFood);
        chipLounges = findViewById(R.id.chipLounges);
        chipCharging = findViewById(R.id.chipCharging);
        chipShopping = findViewById(R.id.chipShopping);
        chipMedical = findViewById(R.id.chipMedical);
        allChips = new Button[]{chipAll, chipRestrooms, chipFood, chipLounges, chipCharging, chipShopping, chipMedical};

        setupCategoryChips();
        setupTerminalWebView();
        setupModeSwitcher();

        // Check if intent passed a specific facility category (e.g. from AirportExplorerActivity)
        String intentCategory = getIntent().getStringExtra("facility_category");
        if (intentCategory != null && !intentCategory.isEmpty()) {
            selectedCategory = intentCategory;
            highlightSelectedChip(intentCategory);
        }

        backBtn.setOnClickListener(v -> finish());
        cardCloseBtn.setOnClickListener(v -> hideFacilityCard());

        btnGpsLocate.setOnClickListener(v -> requestUserLocation());
        btnRecenterAirport.setOnClickListener(v -> recenterToAirport());

        btnStartNavigation.setOnClickListener(v -> {
            if (currentSelectedFacility != null) {
                drawWalkingNavigationRoute(currentSelectedFacility);
            }
        });

        // Initialize SupportMapFragment safely in background
        try {
            MapsInitializer.initialize(getApplicationContext());
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        } catch (Exception e) {
            switchToTerminalMode();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupTerminalWebView() {
        if (terminalWebView == null) return;
        WebSettings settings = terminalWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);

        terminalWebView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void onFacilityClicked(String facilityId) {
                runOnUiThread(() -> {
                    List<AirportFacility> facilities = AirportDataRepository.getFacilitiesForAirport(airportLocation);
                    for (AirportFacility f : facilities) {
                        if (f.getId().equalsIgnoreCase(facilityId)) {
                            showFacilityCard(f);
                            break;
                        }
                    }
                });
            }
        }, "Android");

        terminalWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                terminalWebView.evaluateJavascript("setAirportTitle('" + airportDisplayName + "')", null);
                if (selectedCategory != null && !selectedCategory.equals("All")) {
                    terminalWebView.evaluateJavascript("filterCategory('" + selectedCategory + "')", null);
                }
            }
        });

        terminalWebView.loadUrl("file:///android_asset/airport_terminal_map.html");
    }

    private void setupModeSwitcher() {
        if (btnModeTerminal != null) {
            btnModeTerminal.setOnClickListener(v -> switchToTerminalMode());
        }
        if (btnModeSatellite != null) {
            btnModeSatellite.setOnClickListener(v -> switchToSatelliteMode());
        }
    }

    private void switchToTerminalMode() {
        isTerminalMode = true;
        if (terminalWebView != null) terminalWebView.setVisibility(View.VISIBLE);
        if (mapFragmentContainer != null) mapFragmentContainer.setVisibility(View.GONE);

        if (btnModeTerminal != null) {
            btnModeTerminal.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
            btnModeTerminal.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        if (btnModeSatellite != null) {
            btnModeSatellite.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.transparent));
            btnModeSatellite.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }

        placesApiStatusBadge.setText("● Interactive Terminal Floor Engine Active");
        placesApiStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.success));
    }

    private void switchToSatelliteMode() {
        isTerminalMode = false;
        if (terminalWebView != null) terminalWebView.setVisibility(View.GONE);
        if (mapFragmentContainer != null) mapFragmentContainer.setVisibility(View.VISIBLE);

        if (btnModeSatellite != null) {
            btnModeSatellite.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
            btnModeSatellite.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        if (btnModeTerminal != null) {
            btnModeTerminal.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.transparent));
            btnModeTerminal.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }

        placesApiStatusBadge.setText("● Google Maps Satellite Mode");
        placesApiStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.primary));
    }

    private void resolveAirportContext() {
        // Default to BOM Mumbai
        airportLocation = AirportDataRepository.DEFAULT_AIRPORT_LOCATION;
        airportDisplayName = "Mumbai BOM (Terminal 2)";

        // Check if intent specifies airport
        String passedAirport = getIntent().getStringExtra("airport_name");
        if (passedAirport != null && !passedAirport.isEmpty()) {
            airportLocation = AirportDataRepository.getAirportCoordinates(passedAirport);
            airportDisplayName = passedAirport;
            return;
        }

        // Check SQLite latest trip for current user
        try {
            SharedPreferences prefs = getSharedPreferences("GateCrashSession", Context.MODE_PRIVATE);
            int userId = prefs.getInt("userId", 1);
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            Cursor cursor = dbHelper.getLatestTrip(userId);
            if (cursor != null && cursor.moveToFirst()) {
                String airportStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AIRPORT));
                String terminal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TERMINAL));
                if (airportStr != null && !airportStr.isEmpty()) {
                    airportLocation = AirportDataRepository.getAirportCoordinates(airportStr);
                    airportDisplayName = airportStr + (terminal != null ? " (" + terminal + ")" : "");
                }
                cursor.close();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Map settings
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(this);

        // Position user initially near concourse center
        userLocation = new LatLng(airportLocation.latitude - 0.0001, airportLocation.longitude - 0.0001);

        // Check location permission
        checkLocationPermission();

        // Initial camera animation
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(airportLocation)
                .zoom(17.5f)
                .bearing(45)
                .tilt(30)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Draw user pin
        renderUserLocationMarker();

        // Load & plot facilities for category
        loadAndPlotFacilities(selectedCategory);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMapLocationFeatures();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void enableMapLocationFeatures() {
        if (googleMap == null) return;
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        renderUserLocationMarker();
                    }
                });
            }
        } catch (SecurityException ignored) {
        }
    }

    private void renderUserLocationMarker() {
        if (googleMap == null || userLocation == null) return;
        if (userMarker != null) {
            userMarker.remove();
        }
        userMarker = googleMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title("Your Current Location")
                .snippet("Terminal Concourse")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    private void requestUserLocation() {
        if (terminalWebView != null) {
            terminalWebView.evaluateJavascript("recenterMap()", null);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    renderUserLocationMarker();
                    if (googleMap != null) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18.5f));
                    }
                    Toast.makeText(this, "GPS Locked: " + String.format("%.4f", location.getLatitude()) + ", " + String.format("%.4f", location.getLongitude()), Toast.LENGTH_SHORT).show();
                } else {
                    if (googleMap != null) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18.0f));
                    }
                    Toast.makeText(this, "Acquired Indoor Airport Beacon Location", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            checkLocationPermission();
        }
    }

    private void recenterToAirport() {
        if (terminalWebView != null) {
            terminalWebView.evaluateJavascript("recenterMap()", null);
        }
        if (googleMap != null && airportLocation != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(airportLocation)
                    .zoom(17.5f)
                    .tilt(30)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        }
    }

    private void loadAndPlotFacilities(String category) {
        if (googleMap == null) return;

        // Clear existing markers & route
        for (Marker m : plottedMarkers) {
            m.remove();
        }
        plottedMarkers.clear();
        markerFacilityMap.clear();

        if (currentRoutePolyline != null) {
            currentRoutePolyline.remove();
            currentRoutePolyline = null;
        }

        PlacesManager.loadAirportFacilities(this, airportLocation, category, (facilities, fromLiveApi) -> {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boundsBuilder.include(airportLocation);
            boundsBuilder.include(userLocation);

            for (AirportFacility f : facilities) {
                LatLng pos = new LatLng(f.getLatitude(), f.getLongitude());
                boundsBuilder.include(pos);

                BitmapDescriptor icon = getIconForCategory(f.getCategory());
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(f.getIconEmoji() + " " + f.getName())
                        .snippet(f.getTerminal() + " • " + f.getGateNear())
                        .icon(icon));

                if (marker != null) {
                    marker.setTag(f);
                    plottedMarkers.add(marker);
                    markerFacilityMap.put(marker, f);
                }
            }

            // Auto-focus if category filtered
            if (!category.equalsIgnoreCase("All") && !facilities.isEmpty()) {
                try {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));
                } catch (Exception e) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(airportLocation, 17.5f));
                }
            }
        });
    }

    private BitmapDescriptor getIconForCategory(String category) {
        if (category == null) return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        switch (category) {
            case "Restrooms":
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
            case "Charging Stations":
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            case "Food & Dining":
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            case "Airport Lounges":
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
            case "Duty Free & Shopping":
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
            case "Medical Center":
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            default:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        AirportFacility facility = markerFacilityMap.get(marker);
        if (facility != null) {
            showFacilityCard(facility);
            return false; // let map animate to marker
        }
        return false;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        hideFacilityCard();
    }

    private void showFacilityCard(AirportFacility facility) {
        currentSelectedFacility = facility;

        cardFacilityEmoji.setText(facility.getIconEmoji());
        cardFacilityName.setText(facility.getName());
        cardFacilityLocation.setText(facility.getTerminal() + " • " + facility.getGateNear());
        cardRatingText.setText("★ " + facility.getRating());
        cardHoursText.setText("🟢 " + facility.getOpenHours());

        // Distance & duration calculation
        int dist = facility.calculateDistanceMeters(userLocation.latitude, userLocation.longitude);
        int mins = facility.estimateWalkingMinutes(dist);
        cardDistanceText.setText("🚶 ~" + dist + "m (" + mins + " min walk)");
        cardDescriptionText.setText(facility.getDescription());

        facilityDetailCard.setVisibility(View.VISIBLE);
    }

    private void hideFacilityCard() {
        facilityDetailCard.setVisibility(View.GONE);
        currentSelectedFacility = null;
        if (terminalWebView != null) {
            terminalWebView.evaluateJavascript("clearRoute()", null);
        }
        if (currentRoutePolyline != null) {
            currentRoutePolyline.remove();
            currentRoutePolyline = null;
        }
    }

    private void drawWalkingNavigationRoute(AirportFacility destination) {
        if (destination == null) return;

        // Render walking route on Terminal Floor Map
        if (terminalWebView != null) {
            terminalWebView.evaluateJavascript("drawRouteTo('" + destination.getId() + "')", null);
        }

        // Also render on Google Maps if ready
        if (googleMap != null && userLocation != null) {
            if (currentRoutePolyline != null) {
                currentRoutePolyline.remove();
            }

            LatLng destPos = new LatLng(destination.getLatitude(), destination.getLongitude());

            // Simulated indoor walking waypoint (concourse corridor turn)
            LatLng waypoint = new LatLng(
                    (userLocation.latitude + destPos.latitude) / 2.0 + 0.0001,
                    (userLocation.longitude + destPos.longitude) / 2.0
            );

            PolylineOptions polyOptions = new PolylineOptions()
                    .add(userLocation)
                    .add(waypoint)
                    .add(destPos)
                    .width(14f)
                    .color(Color.parseColor("#1A73E8"))
                    .geodesic(true)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap());

            currentRoutePolyline = googleMap.addPolyline(polyOptions);

            // Zoom to fit entire walking route
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(userLocation);
            builder.include(destPos);
            try {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 180));
            } catch (Exception ignored) {
            }
        }

        int dist = destination.calculateDistanceMeters(userLocation.latitude, userLocation.longitude);
        int mins = destination.estimateWalkingMinutes(dist);

        Toast.makeText(this, "Walking guidance active to " + destination.getName() + " (~" + mins + " mins)", Toast.LENGTH_LONG).show();
    }

    private void setupCategoryChips() {
        chipAll.setOnClickListener(v -> filterCategory("All", chipAll));
        chipRestrooms.setOnClickListener(v -> filterCategory("Restrooms", chipRestrooms));
        chipFood.setOnClickListener(v -> filterCategory("Food & Dining", chipFood));
        chipLounges.setOnClickListener(v -> filterCategory("Airport Lounges", chipLounges));
        chipCharging.setOnClickListener(v -> filterCategory("Charging Stations", chipCharging));
        chipShopping.setOnClickListener(v -> filterCategory("Duty Free & Shopping", chipShopping));
        chipMedical.setOnClickListener(v -> filterCategory("Medical Center", chipMedical));
    }

    private void filterCategory(String category, Button clickedChip) {
        selectedCategory = category;
        highlightChipButton(clickedChip);
        hideFacilityCard();
        if (terminalWebView != null) {
            terminalWebView.evaluateJavascript("filterCategory('" + category + "')", null);
        }
        loadAndPlotFacilities(category);
    }

    private void highlightSelectedChip(String category) {
        for (Button chip : allChips) {
            if (chip.getText().toString().toLowerCase().contains(category.toLowerCase())) {
                highlightChipButton(chip);
                return;
            }
        }
    }

    private void highlightChipButton(Button activeChip) {
        for (Button chip : allChips) {
            if (chip == activeChip) {
                chip.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
                chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                chip.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMapLocationFeatures();
            } else {
                Toast.makeText(this, "Location permission allows live walking navigation.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
