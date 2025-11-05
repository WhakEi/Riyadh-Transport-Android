//
//  SearchResult.swift
//  Riyadh Transport
//
//  Swift model for search results
//

import Foundation
import CoreLocation

struct SearchResult: Identifiable {
    var id: String { UUID().uuidString }
    
    let name: String
    let latitude: Double
    let longitude: Double
    let type: SearchResultType
    
    var coordinate: CLLocationCoordinate2D {
        return CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
}

enum SearchResultType {
    case station
    case location
    case recent
}
