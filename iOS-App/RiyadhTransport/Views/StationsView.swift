import SwiftUI
import MapKit

// Preview wrapper to provide a FocusState binding for the preview
private struct StationsViewPreviewWrapper: View {
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 24.7136, longitude: 46.6753),
        span: MKCoordinateSpan(latitudeDelta: 0.5, longitudeDelta: 0.5)
    )
    @FocusState private var isTextFieldFocused: Bool

    var body: some View {
        StationsView(
            region: $region,
            isTextFieldFocused: $isTextFieldFocused
        )
        .environmentObject(LocationManager())
        .environmentObject(FavoritesManager.shared)
    }
}

#Preview {
    StationsViewPreviewWrapper()
}
