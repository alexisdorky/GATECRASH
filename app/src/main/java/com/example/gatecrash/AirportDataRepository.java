package com.example.gatecrash;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

public class AirportDataRepository {

    // Default airport anchor: BOM Mumbai Terminal 2
    public static final LatLng DEFAULT_AIRPORT_LOCATION = new LatLng(19.0896, 72.8656);

    public static LatLng getAirportCoordinates(String airportNameOrCode) {
        if (airportNameOrCode == null) return DEFAULT_AIRPORT_LOCATION;
        String upper = airportNameOrCode.toUpperCase();
        if (upper.contains("DEL") || upper.contains("DELHI")) {
            return new LatLng(28.5562, 77.1000); // DEL IGI T3
        } else if (upper.contains("BLR") || upper.contains("BENGALURU") || upper.contains("BANGALORE")) {
            return new LatLng(13.1986, 77.7066); // BLR Kempegowda
        } else if (upper.contains("DXB") || upper.contains("DUBAI")) {
            return new LatLng(25.2532, 55.3657); // DXB
        } else if (upper.contains("LHR") || upper.contains("HEATHROW") || upper.contains("LONDON")) {
            return new LatLng(51.4700, -0.4543); // LHR
        } else if (upper.contains("JFK") || upper.contains("NEW YORK")) {
            return new LatLng(40.6413, -73.7781); // JFK
        } else if (upper.contains("SIN") || upper.contains("CHANGI")) {
            return new LatLng(1.3644, 103.9915); // Changi
        }
        // Default BOM Mumbai
        return DEFAULT_AIRPORT_LOCATION;
    }

    public static List<AirportFacility> getFacilitiesForAirport(LatLng baseLocation) {
        List<AirportFacility> facilities = new ArrayList<>();
        double lat = baseLocation.latitude;
        double lng = baseLocation.longitude;

        // 1. Restrooms
        facilities.add(new AirportFacility(
                "restroom_1", "Executive Restrooms & Baby Care", "Restrooms",
                "Terminal 2", "Near Gate B22", lat + 0.0006, lng + 0.0007,
                4.8f, "Open 24 Hours", "Cleaned every 20 mins, includes accessible stalls & baby changing room", "🚻"
        ));
        facilities.add(new AirportFacility(
                "restroom_2", "Concourse A Restrooms", "Restrooms",
                "Terminal 2", "Near Gate A10", lat - 0.0008, lng - 0.0006,
                4.6f, "Open 24 Hours", "Spacious restroom suite with contactless fixtures", "🚻"
        ));
        facilities.add(new AirportFacility(
                "restroom_3", "Arrivals & Baggage Claim Restroom", "Restrooms",
                "Terminal 2", "Belt 4 Area", lat - 0.0003, lng + 0.0011,
                4.5f, "Open 24 Hours", "Located right next to baggage claim exit", "🚻"
        ));

        // 2. Charging Stations
        facilities.add(new AirportFacility(
                "charging_1", "Ultra-Fast Charging Pods & Desks", "Charging Stations",
                "Terminal 2", "Gate B24 Seating", lat + 0.0002, lng + 0.0003,
                4.9f, "Open 24 Hours", "65W USB-C PD + Universal AC outlets at every seat", "🔋"
        ));
        facilities.add(new AirportFacility(
                "charging_2", "TechHub Workstation & Power Bar", "Charging Stations",
                "Terminal 2", "Concourse B Center", lat + 0.0009, lng - 0.0004,
                4.7f, "Open 24 Hours", "High-speed Wi-Fi zone with wireless Qi charging pads", "🔋"
        ));

        // 3. Food & Restaurants
        facilities.add(new AirportFacility(
                "food_1", "Starbucks Reserve Coffee", "Food & Dining",
                "Terminal 2", "Opposite Gate B21", lat + 0.0005, lng - 0.0008,
                4.7f, "Open 24 Hours", "Fresh espresso, cold brew, sandwiches, pastries & power outlets", "☕"
        ));
        facilities.add(new AirportFacility(
                "food_2", "McDonald's & McCafé", "Food & Dining",
                "Terminal 2", "Food Court Level 3", lat - 0.0006, lng + 0.0004,
                4.4f, "Open 24 Hours", "Quick burgers, nuggets, fries and instant self-kiosks", "🍔"
        ));
        facilities.add(new AirportFacility(
                "food_3", "Chai Point & Street Treats", "Food & Dining",
                "Terminal 2", "Near Gate B18", lat + 0.0003, lng - 0.0003,
                4.5f, "05:00 - 01:00", "Hot masala chai, samosas, filter coffee and quick bites", "🍵"
        ));
        facilities.add(new AirportFacility(
                "food_4", "The Irish House Sports Bar", "Food & Dining",
                "Terminal 2", "Central Rotunda", lat + 0.0001, lng + 0.0009,
                4.6f, "11:00 - 03:00", "Draught beers, wings, loaded nachos & live sports", "🍺"
        ));

        // 4. Airport Lounges
        facilities.add(new AirportFacility(
                "lounge_1", "Adani Lounge / Encalm Lounge", "Airport Lounges",
                "Terminal 2", "Mezzanine Level (Gate 44)", lat + 0.0011, lng + 0.0005,
                4.8f, "Open 24 Hours", "Buffet dining, shower suites, bar, nap pods & flight monitors", "🛋"
        ));
        facilities.add(new AirportFacility(
                "lounge_2", "Air India Maharaja Lounge", "Airport Lounges",
                "Terminal 2", "International Wing Gate B", lat - 0.0009, lng + 0.0008,
                4.3f, "Open 24 Hours", "Exclusive lounge for Star Alliance & Maharaja Club members", "🛋"
        ));

        // 5. Duty Free & Shopping
        facilities.add(new AirportFacility(
                "shop_1", "World Duty Free & Perfumes", "Duty Free & Shopping",
                "Terminal 2", "Post-Security Concourse", lat - 0.0001, lng - 0.0007,
                4.6f, "Open 24 Hours", "Tax-free perfumes, cosmetics, chocolates, spirits & electronics", "🛍"
        ));
        facilities.add(new AirportFacility(
                "shop_2", "Relay Books, Travel & Electronics", "Duty Free & Shopping",
                "Terminal 2", "Near Gate B25", lat + 0.0008, lng + 0.0002,
                4.4f, "06:00 - 00:00", "Bestsellers, travel pillows, adaptors, headphones & snacks", "📚"
        ));

        // 6. Medical & Pharmacy
        facilities.add(new AirportFacility(
                "medical_1", "Apollo 24x7 Airport Medical Center", "Medical Center",
                "Terminal 2", "Ground Level (Near Exit 2)", lat - 0.0012, lng - 0.0003,
                4.9f, "Open 24 Hours", "Doctors on duty, oxygen, emergency care & ambulance standby", "🏥"
        ));
        facilities.add(new AirportFacility(
                "medical_2", "Wellness Forever Airport Pharmacy", "Medical Center",
                "Terminal 2", "Near Gate B16", lat + 0.0004, lng + 0.0006,
                4.8f, "Open 24 Hours", "Travel medicine, motion sickness relief, pain meds & sanitizers", "💊"
        ));

        return facilities;
    }

    // Complete list of airports for autocomplete
    public static final String[] POPULAR_AIRPORTS = new String[]{
            "BOM - Chhatrapati Shivaji Maharaj International, Mumbai, India",
            "DEL - Indira Gandhi International, New Delhi, India",
            "BLR - Kempegowda International, Bengaluru, India",
            "HYD - Rajiv Gandhi International, Hyderabad, India",
            "MAA - Chennai International, Chennai, India",
            "CCU - Netaji Subhash Chandra Bose, Kolkata, India",
            "GOI - Dabolim Airport, Goa, India",
            "GOX - Manohar International Airport, Mopa, Goa, India",
            "COK - Cochin International, Kochi, India",
            "AMD - Sardar Vallabhbhai Patel, Ahmedabad, India",
            "PNQ - Pune International Airport, Pune, India",
            "JAI - Jaipur International, Jaipur, India",
            "DXB - Dubai International Airport, UAE",
            "DOH - Hamad International Airport, Doha, Qatar",
            "AUH - Zayed International Airport, Abu Dhabi, UAE",
            "SIN - Singapore Changi Airport, Singapore",
            "KUL - Kuala Lumpur International, Malaysia",
            "BKK - Suvarnabhumi Airport, Bangkok, Thailand",
            "LHR - London Heathrow Airport, London, UK",
            "LGW - London Gatwick Airport, London, UK",
            "CDG - Paris Charles de Gaulle, France",
            "AMS - Amsterdam Schiphol, Netherlands",
            "FRA - Frankfurt Airport, Frankfurt, Germany",
            "JFK - John F. Kennedy International, New York, USA",
            "EWR - Newark Liberty International, New Jersey, USA",
            "ORD - O'Hare International Airport, Chicago, USA",
            "SFO - San Francisco International, California, USA",
            "LAX - Los Angeles International, California, USA",
            "YYZ - Toronto Pearson International, Canada",
            "SYD - Sydney Kingsford Smith Airport, Australia",
            "MEL - Melbourne Airport, Australia",
            "HND - Tokyo Haneda Airport, Tokyo, Japan",
            "NRT - Tokyo Narita International, Japan",
            "ICN - Incheon International, Seoul, South Korea"
    };
}
