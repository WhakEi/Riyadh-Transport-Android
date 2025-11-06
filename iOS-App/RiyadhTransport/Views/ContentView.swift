//
//  ContentView.swift
//  Riyadh Transport
//
//  Main view with tabs and map
//

import SwiftUI
import MapKit

struct ContentView: View {
    @EnvironmentObject var locationManager: LocationManager
    @EnvironmentObject var favoritesManager: FavoritesManager
    @State private var selectedTab = 0
    @State private var showingSettings = false
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 24.7136, longitude: 46.6753), // Riyadh center
        span: MKCoordinateSpan(latitudeDelta: 0.5, longitudeDelta: 0.5)
    )
    @State private var bottomSheetOffset: CGFloat = 0
    @State private var isDragging = false
    @FocusState private var isTextFieldFocused: Bool
    
    // Bottom sheet heights
    private let minHeight: CGFloat = UIScreen.main.bounds.height * 0.5
    private let maxHeight: CGFloat = UIScreen.main.bounds.height * 0.9
    
    var currentHeight: CGFloat {
        return minHeight - bottomSheetOffset
    }
    
    var body: some View {
        NavigationView {
            ZStack {
                // Map background
                MapView(region: $region)
                    .ignoresSafeArea()
                    .onTapGesture {
                        // Dismiss keyboard when tapping map
                        isTextFieldFocused = false
                    }
                
                // Floating action buttons (behind sheet when expanded)
                VStack {
                    HStack {
                        Spacer()
                        
                        VStack(spacing: 16) {
                            // Settings button
                            Button(action: { showingSettings = true }) {
                                Image(systemName: "gear")
                                    .font(.title2)
                                    .foregroundColor(.white)
                                    .frame(width: 56, height: 56)
                                    .background(Color.blue)
                                    .clipShape(Circle())
                                    .shadow(radius: 4)
                            }
                            
                            // Favorites button
                            NavigationLink(destination: FavoritesView()) {
                                Image(systemName: "star.fill")
                                    .font(.title2)
                                    .foregroundColor(.white)
                                    .frame(width: 56, height: 56)
                                    .background(Color.orange)
                                    .clipShape(Circle())
                                    .shadow(radius: 4)
                            }
                        }
                        .padding()
                    }
                    
                    Spacer()
                }
                .padding(.top, 50)
                .opacity(currentHeight < maxHeight * 0.7 ? 1.0 : 0.0)
                .animation(.easeInOut(duration: 0.2), value: currentHeight)
            
            // Bottom sheet with tabs
            VStack(spacing: 0) {
                // Pull handle
                RoundedRectangle(cornerRadius: 3)
                    .fill(Color.gray.opacity(0.4))
                    .frame(width: 40, height: 6)
                    .padding(.top, 8)
                    .padding(.bottom, 8)
                    .gesture(
                        DragGesture()
                            .onChanged { value in
                                isDragging = true
                                let translation = value.translation.height
                                let proposedOffset = -translation
                                
                                // Apply resistance at boundaries for smoother feel
                                let newHeight = minHeight - proposedOffset
                                
                                if newHeight < minHeight {
                                    // Apply resistance when trying to drag below minimum
                                    let excess = minHeight - newHeight
                                    let resistance = excess * 0.3
                                    bottomSheetOffset = -(resistance)
                                } else if newHeight > maxHeight {
                                    // Apply resistance when trying to drag above maximum
                                    let excess = newHeight - maxHeight
                                    let resistance = excess * 0.3
                                    bottomSheetOffset = -(maxHeight - minHeight + resistance)
                                } else {
                                    // Normal dragging
                                    bottomSheetOffset = proposedOffset
                                }
                            }
                            .onEnded { value in
                                isDragging = false
                                let translation = value.translation.height
                                let velocity = value.predictedEndTranslation.height - translation
                                
                                // Calculate target position based on velocity and current position
                                let currentHeight = minHeight - bottomSheetOffset
                                let midPoint = (minHeight + maxHeight) / 2
                                
                                withAnimation(.interpolatingSpring(stiffness: 300, damping: 30)) {
                                    // Strong velocity overrides position
                                    if velocity < -500 {
                                        // Fast upward swipe -> expand
                                        bottomSheetOffset = -(maxHeight - minHeight)
                                    } else if velocity > 500 {
                                        // Fast downward swipe -> collapse
                                        bottomSheetOffset = 0
                                    } else if abs(velocity) > 100 {
                                        // Medium velocity -> direction matters
                                        if velocity < 0 {
                                            bottomSheetOffset = -(maxHeight - minHeight)
                                        } else {
                                            bottomSheetOffset = 0
                                        }
                                    } else {
                                        // Low velocity -> snap to nearest
                                        if currentHeight > midPoint {
                                            bottomSheetOffset = -(maxHeight - minHeight)
                                        } else {
                                            bottomSheetOffset = 0
                                        }
                                    }
                                }
                            }
                    )
                
                // Tab selector
                Picker("Tab", selection: $selectedTab) {
                    Text("route_tab").tag(0)
                    Text("stations_tab").tag(1)
                    Text("lines_tab").tag(2)
                }
                .pickerStyle(.segmented)
                .padding(.horizontal)
                .padding(.bottom, 8)
                
                // Tab content
                TabView(selection: $selectedTab) {
                    RouteView(region: $region, isTextFieldFocused: $isTextFieldFocused)
                        .tag(0)
                    
                    StationsView(region: $region, isTextFieldFocused: $isTextFieldFocused)
                        .tag(1)
                    
                    LinesView()
                        .tag(2)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
            }
            .frame(height: currentHeight)
            .frame(maxWidth: .infinity)
            .background(Color(UIColor.systemBackground))
            .cornerRadius(20, corners: [.topLeft, .topRight])
            .shadow(color: .black.opacity(0.2), radius: 10)
            .offset(y: UIScreen.main.bounds.height - currentHeight)
            .animation(isDragging ? nil : .interpolatingSpring(stiffness: 300, damping: 30), value: bottomSheetOffset)
            .gesture(
                // Allow tapping content to dismiss keyboard
                TapGesture()
                    .onEnded { _ in
                        isTextFieldFocused = false
                    }
            )
        }
        .navigationBarHidden(true)
        .sheet(isPresented: $showingSettings) {
            SettingsView()
        }
        .onAppear {
            locationManager.requestPermission()
        }
    }
}

// Custom corner radius extension
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners
    
    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}

#Preview {
    ContentView()
        .environmentObject(LocationManager())
        .environmentObject(FavoritesManager.shared)
}
