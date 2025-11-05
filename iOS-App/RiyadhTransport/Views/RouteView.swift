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
    @EnvironmentObject var locationManager: LocationManager
    @State private var startLocation: String = ""
    @State private var endLocation: String = ""
    @State private var route: Route?
    @State private var isLoading = false
    @State private var showingError = false
    @State private var errorMessage = ""
    @State private var startCoordinate: CLLocationCoordinate2D?
    @State private var endCoordinate: CLLocationCoordinate2D?
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Start location
                HStack {
                    Image(systemName: "circle.fill")
                        .foregroundColor(.green)
                    TextField("start_location", text: $startLocation)
                        .textFieldStyle(.roundedBorder)
                    Button(action: useCurrentLocation) {
                        Image(systemName: "location.fill")
                    }
                }
                .padding(.horizontal)
                
                // End location
                HStack {
                    Image(systemName: "mappin.circle.fill")
                        .foregroundColor(.red)
                    TextField("end_location", text: $endLocation)
                        .textFieldStyle(.roundedBorder)
                }
                .padding(.horizontal)
                
                // Find route button
                Button(action: findRoute) {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("find_route")
                            .fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
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
    }
    
    private func useCurrentLocation() {
        locationManager.getCurrentLocation { location in
            guard let location = location else { return }
            startCoordinate = location.coordinate
            startLocation = String(format: "%.4f, %.4f", location.coordinate.latitude, location.coordinate.longitude)
        }
    }
    
    private func findRoute() {
        // Parse coordinates from text or use stored coordinates
        guard let startCoord = startCoordinate ?? parseCoordinate(from: startLocation),
              let endCoord = endCoordinate ?? parseCoordinate(from: endLocation) else {
            errorMessage = "Please enter valid coordinates"
            showingError = true
            return
        }
        
        isLoading = true
        
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
                    route = foundRoute
                case .failure(let error):
                    errorMessage = error.localizedDescription
                    showingError = true
                }
            }
        }
    }
    
    private func parseCoordinate(from text: String) -> CLLocationCoordinate2D? {
        let components = text.components(separatedBy: ",")
        guard components.count == 2,
              let lat = Double(components[0].trimmingCharacters(in: .whitespaces)),
              let lng = Double(components[1].trimmingCharacters(in: .whitespaces)) else {
            return nil
        }
        return CLLocationCoordinate2D(latitude: lat, longitude: lng)
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

#Preview {
    RouteView(region: .constant(MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 24.7136, longitude: 46.6753),
        span: MKCoordinateSpan(latitudeDelta: 0.5, longitudeDelta: 0.5)
    )))
    .environmentObject(LocationManager())
}
