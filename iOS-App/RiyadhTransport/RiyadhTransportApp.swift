//
//  RiyadhTransportApp.swift
//  Riyadh Transport
//
//  Main app entry point
//

import SwiftUI

@main
struct RiyadhTransportApp: App {
    @StateObject private var locationManager = LocationManager()
    @StateObject private var favoritesManager = FavoritesManager.shared
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(locationManager)
                .environmentObject(favoritesManager)
        }
    }
}
