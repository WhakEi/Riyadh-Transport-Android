//
//  MapView.swift
//  Riyadh Transport
//
//  Apple Maps view wrapper
//

import SwiftUI
import MapKit

struct MapView: UIViewRepresentable {
    @Binding var region: MKCoordinateRegion
    @State private var stations: [Station] = []
    
    func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = true
        mapView.setRegion(region, animated: false)
        
        // Load and display stations
        loadStations(on: mapView)
        
        return mapView
    }
    
    func updateUIView(_ mapView: MKMapView, context: Context) {
        if mapView.region.center.latitude != region.center.latitude ||
           mapView.region.center.longitude != region.center.longitude {
            mapView.setRegion(region, animated: true)
        }
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    private func loadStations(on mapView: MKMapView) {
        APIService.shared.getStations { result in
            DispatchQueue.main.async {
                switch result {
                case .success(let stations):
                    let annotations = stations.map { station -> MKPointAnnotation in
                        let annotation = MKPointAnnotation()
                        annotation.coordinate = station.coordinate
                        annotation.title = station.displayName
                        annotation.subtitle = station.type?.capitalized
                        return annotation
                    }
                    mapView.addAnnotations(annotations)
                case .failure(let error):
                    print("Error loading stations: \(error.localizedDescription)")
                }
            }
        }
    }
    
    class Coordinator: NSObject, MKMapViewDelegate {
        var parent: MapView
        
        init(_ parent: MapView) {
            self.parent = parent
        }
        
        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            if annotation is MKUserLocation {
                return nil
            }
            
            let identifier = "StationAnnotation"
            var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: identifier) as? MKMarkerAnnotationView
            
            if annotationView == nil {
                annotationView = MKMarkerAnnotationView(annotation: annotation, reuseIdentifier: identifier)
                annotationView?.canShowCallout = true
                annotationView?.rightCalloutAccessoryView = UIButton(type: .detailDisclosure)
            } else {
                annotationView?.annotation = annotation
            }
            
            // Color code by type
            if let subtitle = annotation.subtitle as? String {
                if subtitle.lowercased().contains("metro") {
                    annotationView?.markerTintColor = .systemBlue
                } else if subtitle.lowercased().contains("bus") {
                    annotationView?.markerTintColor = .systemGreen
                }
            }
            
            return annotationView
        }
        
        func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
            parent.region = mapView.region
        }
    }
}
