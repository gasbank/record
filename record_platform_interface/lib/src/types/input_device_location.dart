/// A device-reported physical location, not an inferred microphone position.
///
/// Locations describe input routes; a route may contain multiple microphones.
enum InputDeviceLocation {
  unknown,
  top,
  bottom,
  front,
  back,
  left,
  right;

  static InputDeviceLocation fromString(String? value) =>
      InputDeviceLocation.values.firstWhere(
        (location) => location.name == value,
        orElse: () => InputDeviceLocation.unknown,
      );
}
