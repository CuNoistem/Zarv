package com.example.bluetoothapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;

public class StatsActivity extends AppCompatActivity {

    // Constants for carbon credit equivalents
    private static final double TREES_PER_CREDIT = 50.0;
    private static final double KM_DRIVEN_PER_CREDIT = 4000.0;
    private static final double KWH_SAVED_PER_CREDIT = 1200.0;
    private static final double FLIGHTS_PER_CREDIT = 4.0;
    private static final double LITERS_GASOLINE_PER_CREDIT = 400.0;

    // UI components
    private TextView totalCarbonCreditsValue;
    private TextView treesPlantedValueTextView;
    private TextView treesPlantedDescTextView;
    private TextView drivingDistanceValueTextView;
    private TextView drivingDistanceDescTextView;
    private TextView energySavedValueTextView;
    private TextView energySavedDescTextView;
    private TextView flightsAvoidedValueTextView;
    private TextView flightsAvoidedDescTextView;
    private TextView gasolineSavedValueTextView;
    private TextView gasolineSavedDescTextView;

    // Formatter for nice number display
    private DecimalFormat formatter = new DecimalFormat("#,###.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stats);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        initializeViews();

        // Get data from intent
        double carbonCredits = getCarbonCreditsFromIntent();

        // Update UI with calculated values
        updateUI(carbonCredits);
    }

    private void initializeViews() {
        totalCarbonCreditsValue = findViewById(R.id.totalCarbonCreditsValue);

        treesPlantedValueTextView = findViewById(R.id.treesPlantedValueTextView);
        treesPlantedDescTextView = findViewById(R.id.treesPlantedDescTextView);

        drivingDistanceValueTextView = findViewById(R.id.drivingDistanceValueTextView);
        drivingDistanceDescTextView = findViewById(R.id.drivingDistanceDescTextView);

        energySavedValueTextView = findViewById(R.id.energySavedValueTextView);
        energySavedDescTextView = findViewById(R.id.energySavedDescTextView);

        flightsAvoidedValueTextView = findViewById(R.id.flightsAvoidedValueTextView);
        flightsAvoidedDescTextView = findViewById(R.id.flightsAvoidedDescTextView);

        gasolineSavedValueTextView = findViewById(R.id.gasolineSavedValueTextView);
        gasolineSavedDescTextView = findViewById(R.id.gasolineSavedDescTextView);
    }

    private double getCarbonCreditsFromIntent() {
        // Get carbon credits from intent
        String carbonCreditsStr = getIntent().getStringExtra("carbonCredits");
        double carbonCredits = 0.0;

        try {
            // Try to parse the string value
            if (carbonCreditsStr != null && !carbonCreditsStr.equals("--")) {
                carbonCredits = Double.parseDouble(carbonCreditsStr);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // If there's an error, default to 0
            carbonCredits = 0.0;
        }

        return carbonCredits;
    }

    private void updateUI(double carbonCredits) {
        // Update total carbon credits display
        totalCarbonCreditsValue.setText(formatter.format(carbonCredits));

        // Calculate and update trees planted
        double treesPlanted = carbonCredits * TREES_PER_CREDIT;
        treesPlantedValueTextView.setText(formatter.format(treesPlanted) + " trees");
        updateTreesDescription(treesPlanted);

        // Calculate and update driving distance
        double kmDriven = carbonCredits * KM_DRIVEN_PER_CREDIT;
        drivingDistanceValueTextView.setText(formatter.format(kmDriven) + " km");
        updateDrivingDescription(kmDriven);

        // Calculate and update energy saved
        double kwhSaved = carbonCredits * KWH_SAVED_PER_CREDIT;
        energySavedValueTextView.setText(formatter.format(kwhSaved) + " kWh");
        updateEnergySavedDescription(kwhSaved);

        // Calculate and update flights avoided
        double flightsAvoided = carbonCredits * FLIGHTS_PER_CREDIT;
        flightsAvoidedValueTextView.setText(formatter.format(flightsAvoided) + " flights");
        updateFlightsDescription(flightsAvoided);

        // Calculate and update gasoline saved
        double gasolineSaved = carbonCredits * LITERS_GASOLINE_PER_CREDIT;
        gasolineSavedValueTextView.setText(formatter.format(gasolineSaved) + " liters");
        updateGasolineDescription(gasolineSaved);
    }

    private void updateTreesDescription(double treesPlanted) {
        String description;

        if (treesPlanted <= 10) {
            description = "Equivalent to a small garden";
        } else if (treesPlanted <= 100) {
            description = "Equivalent to a small park";
        } else if (treesPlanted <= 1000) {
            description = "Equivalent to a city park";
        } else if (treesPlanted <= 10000) {
            description = "Equivalent to a small forest";
        } else {
            description = "Equivalent to a large conservation area";
        }

        treesPlantedDescTextView.setText(description);
    }

    private void updateDrivingDescription(double kmDriven) {
        String description;

        if (kmDriven <= 500) {
            description = "Equivalent to a road trip between nearby cities";
        } else if (kmDriven <= 3000) {
            description = "Equivalent to driving across a small country";
        } else if (kmDriven <= 10000) {
            description = "More than driving across the United States";
        } else if (kmDriven <= 40000) {
            description = "Equivalent to driving around Earth's circumference";
        } else {
            description = "Equivalent to multiple trips around the world";
        }

        drivingDistanceDescTextView.setText(description);
    }

    private void updateEnergySavedDescription(double kwhSaved) {
        String description;

        if (kwhSaved <= 500) {
            description = "Powers an average home for about 2 months";
        } else if (kwhSaved <= 3000) {
            description = "Powers an average home for about a year";
        } else if (kwhSaved <= 10000) {
            description = "Powers several homes for a year";
        } else if (kwhSaved <= 50000) {
            description = "Powers a small neighborhood for a year";
        } else {
            description = "Powers a significant portion of a community";
        }

        energySavedDescTextView.setText(description);
    }

    private void updateFlightsDescription(double flightsAvoided) {
        String description;

        if (flightsAvoided <= 5) {
            description = "Equivalent to a few domestic flights";
        } else if (flightsAvoided <= 20) {
            description = "Equivalent to several international flights";
        } else if (flightsAvoided <= 100) {
            description = "Equivalent to the yearly flights of a frequent traveler";
        } else {
            description = "Equivalent to a significant portion of an airplane's yearly flights";
        }

        flightsAvoidedDescTextView.setText(description);
    }

    private void updateGasolineDescription(double litersSaved) {
        String description;

        if (litersSaved <= 100) {
            description = "Equivalent to a few tanks of gas";
        } else if (litersSaved <= 1000) {
            description = "Equivalent to fuel for a road trip across the country";
        } else if (litersSaved <= 10000) {
            description = "Equivalent to a year's worth of driving for several cars";
        } else {
            description = "Equivalent to a small gas station's daily sales";
        }

        gasolineSavedDescTextView.setText(description);
    }
}