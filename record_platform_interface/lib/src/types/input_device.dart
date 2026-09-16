import 'input_device_type.dart';
import 'input_device_location.dart';

class InputDevice {
  /// The ID used to select the device on the platform.
  final String id;

  /// The label text representation.
  final String label;

  /// The transport type of this device.
  ///
  /// Defaults to [InputDeviceType.unknown] on platforms that do not report
  /// device types (Linux, Web).
  final InputDeviceType type;

  /// The reported location of this input route.
  ///
  /// Unknown when the platform or device cannot identify its location.
  final InputDeviceLocation location;

  /// The sample rates supported or prefered by this device.
  ///
  /// Empty if the platform does not report per-device sample rates (iOS, Web).
  final List<int> sampleRates;

  const InputDevice({
    required this.id,
    required this.label,
    this.type = InputDeviceType.unknown,
    this.location = InputDeviceLocation.unknown,
    this.sampleRates = const [],
  });

  factory InputDevice.fromMap(Map map) => InputDevice(
    id: map['id'],
    label: map['label'],
    type: InputDeviceType.fromString(map['type'] as String?),
    location: InputDeviceLocation.fromString(map['location'] as String?),
    sampleRates: List<int>.from(map['sampleRates'] as List? ?? const []),
  );

  Map<String, dynamic> toMap() => {
    'id': id,
    'label': label,
    'type': type.name,
    'location': location.name,
    if (sampleRates.isNotEmpty) 'sampleRates': sampleRates,
  };

  @override
  String toString() {
    return '''
      id: $id
      label: $label
      type: $type
      location: $location
      sampleRates: $sampleRates
      ''';
  }

  @override
  bool operator ==(Object other) {
    if (identical(this, other)) return true;

    return other is InputDevice && other.id == id && other.label == label;
  }

  @override
  int get hashCode => id.hashCode ^ label.hashCode;
}
