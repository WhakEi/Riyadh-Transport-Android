//
//  StationsView.swift
//  Riyadh Transport
//
//  Stations list view
//

import SwiftUI
import MapKit

struct StationsView: View {
    @Binding var region: MKCoordinateRegion
    @EnvironmentObject var locationManager: LocationManager
    @State private var stations: [Station] = []
    @State private var searchText = ""
    @State private var isLoading = false
    @State private var showingNearby = false
    
    var filteredStations: [Station] {
        if searchText.isEmpty {
            return stations
        }
        return stations.filter { station in
            station.displayName.localizedCaseInsensitiveContains(searchText)
        }
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Search bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.gray)
                TextField("search_stations", text: $searchText)
                    .textFieldStyle(.plain)
                if !searchText.isEmpty {
                    Button(action: { searchText = "" }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.gray)
                    }
                }
            }
            .padding(8)
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(10)
            .padding()
            
            // Nearby button
            Button(action: loadNearbyStations) {
                HStack {
                    Image(systemName: "location.fill")
                    Text("nearby_stations")
                }
            }
            .padding(.horizontal)
            .padding(.bottom, 8)
            
            // Stations list
            if isLoading {
                ProgressView()
                    .padding()
            } else {
                List(filteredStations) { station in
                    NavigationLink(destination: StationDetailView(station: station)) {
                        StationRow(station: station)
                    }
                }
                .listStyle(.plain)
            }
        }
        .onAppear(perform: loadStations)
    }
    
    private func loadStations() {
        isLoading = true
        APIService.shared.getStations { result in
            DispatchQueue.main.async {
                isLoading = false
                switch result {
                case .success(let loadedStations):
                    stations = loadedStations
                case .failure(let error):
                    print("Error loading stations: \(error.localizedDescription)")
                }
            }
        }
    }
    
    private func loadNearbyStations() {
        guard let location = locationManager.location else {
            locationManager.getCurrentLocation { _ in
                loadNearbyStations()
            }
            return
        }
        
        isLoading = true
        showingNearby = true
        
        APIService.shared.getNearbyStations(
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude
        ) { result in
            DispatchQueue.main.async {
                isLoading = false
                switch result {
                case .success(let nearbyStations):
                    stations = nearbyStations
                case .failure(let error):
                    print("Error loading nearby stations: \(error.localizedDescription)")
                }
            }
        }
    }
}

struct StationRow: View {
    let station: Station
    @EnvironmentObject var favoritesManager: FavoritesManager
    
    var body: some View {
        HStack {
            // Icon
            Circle()
                .fill(station.isMetro ? Color.blue : Color.green)
                .frame(width: 40, height: 40)
                .overlay(
                    Image(systemName: station.isMetro ? "tram.fill" : "bus.fill")
                        .foregroundColor(.white)
                        .font(.system(size: 20))
                )
            
            // Name and type
            VStack(alignment: .leading, spacing: 4) {
                Text(station.displayName)
                    .font(.headline)
                Text(station.type?.capitalized ?? "Station")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                
                if let distance = station.distance {
                    Text("\(Int(distance)) m")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            
            Spacer()
            
            // Favorite button
            Button(action: {
                if favoritesManager.isFavoriteStation(station) {
                    favoritesManager.removeFavoriteStation(station)
                } else {
                    favoritesManager.addFavoriteStation(station)
                }
            }) {
                Image(systemName: favoritesManager.isFavoriteStation(station) ? "star.fill" : "star")
                    .foregroundColor(.orange)
            }
            .buttonStyle(.plain)
        }
        .padding(.vertical, 4)
    }
}

#Preview {
    StationsView(region: .constant(MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 24.7136, longitude: 46.6753),
        span: MKCoordinateSpan(latitudeDelta: 0.5, longitudeDelta: 0.5)
    )))
    .environmentObject(LocationManager())
    .environmentObject(FavoritesManager.shared)
}
