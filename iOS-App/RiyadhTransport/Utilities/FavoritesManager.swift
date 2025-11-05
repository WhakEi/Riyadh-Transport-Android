//
//  FavoritesManager.swift
//  Riyadh Transport
//
//  Manager for favorite locations and stations
//

import Foundation
import CoreLocation

class FavoritesManager: ObservableObject {
    static let shared = FavoritesManager()
    
    @Published var favoriteStations: [Station] = []
    @Published var favoriteLocations: [SearchResult] = []
    @Published var searchHistory: [SearchResult] = []
    
    private let favoritesKey = "FavoriteStations"
    private let locationsKey = "FavoriteLocations"
    private let historyKey = "SearchHistory"
    private let maxHistoryItems = 10
    
    private init() {
        loadFavorites()
    }
    
    // MARK: - Stations
    
    func addFavoriteStation(_ station: Station) {
        if !favoriteStations.contains(where: { $0.id == station.id }) {
            favoriteStations.append(station)
            saveFavorites()
        }
    }
    
    func removeFavoriteStation(_ station: Station) {
        favoriteStations.removeAll { $0.id == station.id }
        saveFavorites()
    }
    
    func isFavoriteStation(_ station: Station) -> Bool {
        return favoriteStations.contains { $0.id == station.id }
    }
    
    // MARK: - Locations
    
    func addFavoriteLocation(_ location: SearchResult) {
        if !favoriteLocations.contains(where: { $0.name == location.name }) {
            favoriteLocations.append(location)
            saveLocations()
        }
    }
    
    func removeFavoriteLocation(_ location: SearchResult) {
        favoriteLocations.removeAll { $0.name == location.name }
        saveLocations()
    }
    
    func isFavoriteLocation(_ location: SearchResult) -> Bool {
        return favoriteLocations.contains { $0.name == location.name }
    }
    
    // MARK: - Search History
    
    func addToSearchHistory(_ result: SearchResult) {
        // Remove if already exists
        searchHistory.removeAll { $0.name == result.name }
        
        // Add to beginning
        searchHistory.insert(result, at: 0)
        
        // Limit size
        if searchHistory.count > maxHistoryItems {
            searchHistory = Array(searchHistory.prefix(maxHistoryItems))
        }
        
        saveHistory()
    }
    
    func clearSearchHistory() {
        searchHistory.removeAll()
        saveHistory()
    }
    
    // MARK: - Persistence
    
    private func saveFavorites() {
        if let encoded = try? JSONEncoder().encode(favoriteStations) {
            UserDefaults.standard.set(encoded, forKey: favoritesKey)
        }
    }
    
    private func saveLocations() {
        // Note: SearchResult is not Codable, so we'll need a simple representation
        let locationData = favoriteLocations.map { ["name": $0.name, "lat": $0.latitude, "lng": $0.longitude] }
        if let encoded = try? JSONSerialization.data(withJSONObject: locationData) {
            UserDefaults.standard.set(encoded, forKey: locationsKey)
        }
    }
    
    private func saveHistory() {
        let historyData = searchHistory.map { ["name": $0.name, "lat": $0.latitude, "lng": $0.longitude] }
        if let encoded = try? JSONSerialization.data(withJSONObject: historyData) {
            UserDefaults.standard.set(encoded, forKey: historyKey)
        }
    }
    
    private func loadFavorites() {
        if let data = UserDefaults.standard.data(forKey: favoritesKey),
           let stations = try? JSONDecoder().decode([Station].self, from: data) {
            favoriteStations = stations
        }
        
        if let data = UserDefaults.standard.data(forKey: locationsKey),
           let locationData = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] {
            favoriteLocations = locationData.compactMap { dict in
                guard let name = dict["name"] as? String,
                      let lat = dict["lat"] as? Double,
                      let lng = dict["lng"] as? Double else { return nil }
                return SearchResult(name: name, latitude: lat, longitude: lng, type: .location)
            }
        }
        
        if let data = UserDefaults.standard.data(forKey: historyKey),
           let historyData = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] {
            searchHistory = historyData.compactMap { dict in
                guard let name = dict["name"] as? String,
                      let lat = dict["lat"] as? Double,
                      let lng = dict["lng"] as? Double else { return nil }
                return SearchResult(name: name, latitude: lat, longitude: lng, type: .recent)
            }
        }
    }
}
