//
//  StationsView.swift
//  Riyadh Transport
//
//  Browse and search stations
//

import SwiftUI
import MapKit

struct StationsView: View {
    @Binding var region: MKCoordinateRegion
    @FocusState.Binding var isTextFieldFocused: Bool
    @State private var stations: [Station] = []
    @State private var searchText = ""
    @State private var isLoading = false
    
    var filteredStations: [Station] {
        if searchText.isEmpty {
            return stations
        } else {
            return stations.filter {
                $0.displayName.localizedCaseInsensitiveContains(searchText)
            }
        }
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Search bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.gray)
                TextField("search_station", text: $searchText)
                    .textFieldStyle(.plain)
                    .focused($isTextFieldFocused)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
                
                if !searchText.isEmpty {
                    Button(action: { searchText = "" }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.gray)
                    }
                }
            }
            .padding(12)
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(10)
            .padding()
            
            if isLoading {
                ProgressView()
                    .padding()
            } else if filteredStations.isEmpty {
                VStack(spacing: 16) {
                    Image(systemName: "tram.fill")
                        .font(.system(size: 50))
                        .foregroundColor(.gray)
                    Text("no_stations")
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List(filteredStations) { station in
                    NavigationLink(destination: StationDetailView(station: station)) {
                        HStack {
                            Image(systemName: station.isMetro ? "tram.fill" : "bus.fill")
                                .foregroundColor(station.isMetro ? .blue : .green)
                                .frame(width: 30)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(station.displayName)
                                    .font(.headline)
                                Text(String(format: "%.4f, %.4f", station.latitude, station.longitude))
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            Spacer()
                        }
                        .contentShape(Rectangle())
                        .onTapGesture {
                            // Optionally zoom to station on map
                            withAnimation {
                                region.center = station.coordinate
                            }
                            isTextFieldFocused = false
                        }
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
}
