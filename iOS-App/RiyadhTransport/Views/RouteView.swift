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
    @State private var showingStartSearch = false
    @State private var showingEndSearch = false
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Start location
                HStack {
                    Image(systemName: "circle.fill")
                        .foregroundColor(.green)
                    TextField("start_location", text: $startLocation)
                        .textFieldStyle(.roundedBorder)
                        .focused($isTextFieldFocused)
                        .onTapGesture {
                            showingStartSearch = true
                        }
                        .disabled(true) // Disable direct editing
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
                        .focused($isTextFieldFocused)
                        .onTapGesture {
                            showingEndSearch = true
                        }
                        .disabled(true) // Disable direct editing
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
        .sheet(isPresented: $showingStartSearch) {
            SearchLocationView(isPresented: $showingStartSearch) { result in
                startLocation = result.name
                startCoordinate = result.coordinate
            }
        }
        .sheet(isPresented: $showingEndSearch) {
            SearchLocationView(isPresented: $showingEndSearch) { result in
                endLocation = result.name
                endCoordinate = result.coordinate
            }
        }
    }
    
    private func useCurrentLocation() {
        locationManager.getCurrentLocation { location in
            guard let location = location else { return }
            startCoordinate = location.coordinate
            startLocation = NSLocalizedString("my_location", comment: "My Location")
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

#Preview {
    RouteView(region: .constant(MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 24.7136, longitude: 46.6753),
        span: MKCoordinateSpan(latitudeDelta: 0.5, longitudeDelta: 0.5)
    )))
    .environmentObject(LocationManager())
}
