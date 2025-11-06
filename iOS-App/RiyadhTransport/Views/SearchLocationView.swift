//
//  SearchLocationView.swift
//  Riyadh Transport
//
//  Search location with autocomplete
//

import SwiftUI
import MapKit

struct SearchLocationView: View {
    @Binding var isPresented: Bool
    let onSelect: (SearchResult) -> Void
    
    @State private var searchText = ""
    @State private var searchResults: [SearchResult] = []
    @State private var stations: [Station] = []
    @State private var isSearching = false
    @State private var searchTask: Task<Void, Never>?
    
    var filteredStations: [SearchResult] {
        guard !searchText.isEmpty else { return [] }
        return stations
            .filter { $0.displayName.localizedCaseInsensitiveContains(searchText) }
            .prefix(5)
            .map { station in
                SearchResult(
                    name: station.displayName,
                    latitude: station.latitude,
                    longitude: station.longitude,
                    type: .station
                )
            }
    }
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Search bar
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.gray)
                    TextField("search_location", text: $searchText)
                        .textFieldStyle(.plain)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                        .onChange(of: searchText) { newValue in
                            performSearch(query: newValue)
                        }
                    
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
                
                // Results list
                if isSearching {
                    ProgressView()
                        .padding()
                } else if searchText.isEmpty {
                    VStack(spacing: 16) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 50))
                            .foregroundColor(.gray)
                        Text("search_hint")
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        // Show stations first
                        if !filteredStations.isEmpty {
                            Section(header: Text("stations")) {
                                ForEach(filteredStations) { result in
                                    SearchResultRow(result: result)
                                        .contentShape(Rectangle())
                                        .onTapGesture {
                                            selectResult(result)
                                        }
                                }
                            }
                        }
                        
                        // Show location search results
                        if !searchResults.isEmpty {
                            Section(header: Text("locations")) {
                                ForEach(searchResults) { result in
                                    SearchResultRow(result: result)
                                        .contentShape(Rectangle())
                                        .onTapGesture {
                                            selectResult(result)
                                        }
                                }
                            }
                        }
                        
                        // Show empty state if no results
                        if filteredStations.isEmpty && searchResults.isEmpty {
                            Text("no_results")
                                .foregroundColor(.secondary)
                                .frame(maxWidth: .infinity, alignment: .center)
                                .padding()
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("search_location")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("cancel") {
                        isPresented = false
                    }
                }
            }
        }
        .onAppear {
            loadStations()
        }
        .onDisappear {
            searchTask?.cancel()
        }
    }
    
    private func loadStations() {
        APIService.shared.getStations { result in
            DispatchQueue.main.async {
                switch result {
                case .success(let loadedStations):
                    stations = loadedStations
                    print("Loaded \(stations.count) stations")
                case .failure(let error):
                    print("Error loading stations: \(error.localizedDescription)")
                }
            }
        }
    }
    
    private func performSearch(query: String) {
        // Cancel previous search
        searchTask?.cancel()
        
        guard !query.isEmpty, query.count >= 3 else {
            searchResults = []
            return
        }
        
        // Debounce search
        searchTask = Task {
            try? await Task.sleep(nanoseconds: 500_000_000) // 0.5 second delay
            
            guard !Task.isCancelled else { return }
            
            await MainActor.run {
                isSearching = true
            }
            
            APIService.shared.searchLocation(query: query) { result in
                Task { @MainActor in
                    isSearching = false
                    
                    guard !Task.isCancelled else { return }
                    
                    switch result {
                    case .success(let results):
                        searchResults = results.map { nominatim in
                            SearchResult(
                                name: nominatim.displayName,
                                latitude: nominatim.coordinate.latitude,
                                longitude: nominatim.coordinate.longitude,
                                type: .location
                            )
                        }
                        print("Found \(searchResults.count) location results")
                    case .failure(let error):
                        print("Search error: \(error.localizedDescription)")
                        searchResults = []
                    }
                }
            }
        }
    }
    
    private func selectResult(_ result: SearchResult) {
        onSelect(result)
        isPresented = false
    }
}

struct SearchResultRow: View {
    let result: SearchResult
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: result.type == .station ? "tram.fill" : "mappin.circle.fill")
                .foregroundColor(result.type == .station ? .blue : .red)
                .frame(width: 30)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(result.name)
                    .font(.body)
                    .lineLimit(2)
                
                Text(String(format: "%.4f, %.4f", result.latitude, result.longitude))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
        }
        .padding(.vertical, 4)
    }
}

#Preview {
    SearchLocationView(isPresented: .constant(true)) { _ in }
}
