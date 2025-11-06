//
//  RouteView.swift
//  Riyadh Transport
//
//  Route planning view
//

import SwiftUI
import MapKit

struct RouteView: View {
    @Binding var region: MKCoordinateRegion
    @FocusState.Binding var isTextFieldFocused: Bool
    @EnvironmentObject var locationManager: LocationManager
    @State private var startLocation: String = ""
    @State private var endLocation: String = ""
    @State private var route: Route?
    @State private var isLoading = false
    @State private var showingError = false
    @State private var errorMessage = ""
    @State private var startCoordinate: CLLocationCoordinate2D?
    @State private var endCoordinate: CLLocationCoordinate2D?
    @State private var showStartSuggestions = false
    @State private var showEndSuggestions = false
    @State private var stations: [Station] = []
    @State private var isLoadingStations = false
    @FocusState private var startFieldFocused: Bool
    @FocusState private var endFieldFocused: Bool
    
    var startSuggestions: [Station] {
        guard !startLocation.isEmpty else { return [] }
        return stations
            .filter { $0.displayName.localizedCaseInsensitiveContains(startLocation) }
            .prefix(5)
            .map { $0 }
    }
    
    var endSuggestions: [Station] {
        guard !endLocation.isEmpty else { return [] }
        return stations
            .filter { $0.displayName.localizedCaseInsensitiveContains(endLocation) }
            .prefix(5)
            .map { $0 }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Start location with autocomplete
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 12) {
                        Image(systemName: "circle.fill")
                            .foregroundColor(.green)
                            .font(.system(size: 14))
                        
                        TextField("start_location", text: $startLocation)
                            .font(.system(size: 16))
                            .padding(.vertical, 12)
                            .padding(.horizontal, 12)
                            .background(Color(UIColor.secondarySystemBackground))
                            .cornerRadius(10)
                            .focused($startFieldFocused)
                            .onChange(of: startLocation) { _ in
                                showStartSuggestions = !startLocation.isEmpty && startFieldFocused
                                startCoordinate = nil
                            }
                            .onChange(of: startFieldFocused) { focused in
                                showStartSuggestions = focused && !startLocation.isEmpty
                            }
                        
                        Button(action: useCurrentLocation) {
                            Image(systemName: "location.fill")
                                .font(.system(size: 18))
                                .foregroundColor(.blue)
                                .padding(8)
                        }
                    }
                    
                    // Start suggestions
                    if showStartSuggestions && !startSuggestions.isEmpty {
                        VStack(spacing: 0) {
                            ForEach(startSuggestions) { station in
                                Button(action: {
                                    selectStartStation(station)
                                }) {
                                    HStack {
                                        Image(systemName: station.isMetro ? "tram.fill" : "bus.fill")
                                            .foregroundColor(station.isMetro ? .blue : .green)
                                            .font(.system(size: 14))
                                        
                                        Text(station.displayName)
                                            .font(.system(size: 15))
                                            .foregroundColor(.primary)
                                        
                                        Spacer()
                                    }
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 10)
                                    .background(Color(UIColor.tertiarySystemBackground))
                                }
                                
                                if station.id != startSuggestions.last?.id {
                                    Divider()
                                        .padding(.leading, 40)
                                }
                            }
                        }
                        .background(Color(UIColor.tertiarySystemBackground))
                        .cornerRadius(10)
                        .padding(.leading, 26)
                        .shadow(color: .black.opacity(0.1), radius: 3, x: 0, y: 2)
                    }
                }
                .padding(.horizontal)
                
                // End location with autocomplete
                VStack(alignment: .leading, spacing: 8) {
                    HStack(spacing: 12) {
                        Image(systemName: "mappin.circle.fill")
                            .foregroundColor(.red)
                            .font(.system(size: 14))
                        
                        TextField("end_location", text: $endLocation)
                            .font(.system(size: 16))
                            .padding(.vertical, 12)
                            .padding(.horizontal, 12)
                            .background(Color(UIColor.secondarySystemBackground))
                            .cornerRadius(10)
                            .focused($endFieldFocused)
                            .onChange(of: endLocation) { _ in
                                showEndSuggestions = !endLocation.isEmpty && endFieldFocused
                                endCoordinate = nil
                            }
                            .onChange(of: endFieldFocused) { focused in
                                showEndSuggestions = focused && !endLocation.isEmpty
                            }
                    }
                    
                    // End suggestions
                    if showEndSuggestions && !endSuggestions.isEmpty {
                        VStack(spacing: 0) {
                            ForEach(endSuggestions) { station in
                                Button(action: {
                                    selectEndStation(station)
                                }) {
                                    HStack {
                                        Image(systemName: station.isMetro ? "tram.fill" : "bus.fill")
                                            .foregroundColor(station.isMetro ? .blue : .green)
                                            .font(.system(size: 14))
                                        
                                        Text(station.displayName)
                                            .font(.system(size: 15))
                                            .foregroundColor(.primary)
                                        
                                        Spacer()
                                    }
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 10)
                                    .background(Color(UIColor.tertiarySystemBackground))
                                }
                                
                                if station.id != endSuggestions.last?.id {
                                    Divider()
                                        .padding(.leading, 40)
                                }
                            }
                        }
                        .background(Color(UIColor.tertiarySystemBackground))
                        .cornerRadius(10)
                        .padding(.leading, 26)
                        .shadow(color: .black.opacity(0.1), radius: 3, x: 0, y: 2)
                    }
                }
                .padding(.horizontal)

                // Find route button
                Button(action: findRoute) {
                    HStack {
                        if isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("find_route")
                                .fontWeight(.semibold)
                                .font(.system(size: 17))
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                }
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(12)
                .padding(.horizontal)
                .disabled(isLoading)

                // Route results
                if let route = route {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("route_details")
                            .font(.headline)
                            .padding(.horizontal)

                        Text("total_time: \(route.totalMinutes) min")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .padding(.horizontal)

                        ForEach(route.segments) { segment in
                            RouteSegmentRow(segment: segment)
                        }
                    }
                    .padding(.top)
                }
            }
            .padding(.vertical)
        }
        .alert("Error", isPresented: $showingError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(errorMessage)
        }
        .onAppear {
            loadStations()
        }
    }
    
    private func loadStations() {
        guard stations.isEmpty else { return }
        isLoadingStations = true
        
        APIService.shared.getStations { result in
            DispatchQueue.main.async {
                isLoadingStations = false
                switch result {
                case .success(let loadedStations):
                    stations = loadedStations
                    print("Loaded \(stations.count) stations for autocomplete")
                case .failure(let error):
                    print("Error loading stations: \(error.localizedDescription)")
                }
            }
        }
    }
    
    private func selectStartStation(_ station: Station) {
        startLocation = station.displayName
        startCoordinate = station.coordinate
        showStartSuggestions = false
        startFieldFocused = false
    }
    
    private func selectEndStation(_ station: Station) {
        endLocation = station.displayName
        endCoordinate = station.coordinate
        showEndSuggestions = false
        endFieldFocused = false
    }
    
    private func useCurrentLocation() {
        locationManager.getCurrentLocation { location in
            guard let location = location else { return }
            startCoordinate = location.coordinate
            startLocation = NSLocalizedString("my_location", comment: "My Location")
            showStartSuggestions = false
        }
    }
    
    private func findRoute() {
        // Check if we have coordinates
        guard let startCoord = startCoordinate,
              let endCoord = endCoordinate else {
            errorMessage = NSLocalizedString("select_locations", comment: "Please select start and end locations")
            showingError = true
            return
        }
        
        isLoading = true
        
        print("Finding route from \(startCoord.latitude), \(startCoord.longitude) to \(endCoord.latitude), \(endCoord.longitude)")
        
        APIService.shared.findRoute(
            startLat: startCoord.latitude,
            startLng: startCoord.longitude,
            endLat: endCoord.latitude,
            endLng: endCoord.longitude
        ) { result in
            DispatchQueue.main.async {
                isLoading = false
                switch result {
                case .success(let foundRoute):
                    print("Route found with \(foundRoute.segments.count) segments")
                    route = foundRoute
                case .failure(let error):
                    print("Route error: \(error.localizedDescription)")
                    errorMessage = error.localizedDescription
                    showingError = true
                }
            }
        }
    }
}

struct RouteSegmentRow: View {
    let segment: RouteSegment

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // Icon
            Circle()
                .fill(LineColorHelper.getColorForSegment(type: segment.type, line: segment.line))
                .frame(width: 40, height: 40)
                .overlay(
                    Image(systemName: iconForSegment)
                        .foregroundColor(.white)
                )

            // Details
            VStack(alignment: .leading, spacing: 4) {
                Text(segment.isWalking ? "walk" : segment.line ?? "")
                    .font(.headline)

                if let stations = segment.stations, !stations.isEmpty {
                    Text("\(stations.count) stops")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }

                Text("\(Int(segment.durationInSeconds / 60)) min")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Spacer()
        }
        .padding()
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(10)
        .padding(.horizontal)
    }

    private var iconForSegment: String {
        if segment.isWalking {
            return "figure.walk"
        } else if segment.isMetro {
            return "tram.fill"
        } else if segment.isBus {
            return "bus.fill"
        }
        return "arrow.right"
    }
}

// Wrapper for preview to provide a FocusState binding
private struct RouteViewPreviewWrapper: View {
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 24.7136, longitude: 46.6753),
        span: MKCoordinateSpan(latitudeDelta: 0.5, longitudeDelta: 0.5)
    )
    @FocusState private var isTextFieldFocused: Bool

    var body: some View {
        RouteView(
            region: $region,
            isTextFieldFocused: $isTextFieldFocused
        )
        .environmentObject(LocationManager())
    }
}

#Preview {
    RouteViewPreviewWrapper()
}
