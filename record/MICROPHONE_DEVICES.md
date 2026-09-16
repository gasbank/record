# Microphone metadata

The listInputDevices() method returns input routes, not necessarily individual
physical microphone capsules. Device IDs and selection behavior are unchanged.

InputDevice.location defaults to InputDeviceLocation.unknown. Android 28+
normalizes explicit built-in microphone addresses (top/upper, bottom/lower,
front, back/rear, left, right). OEM addresses are not standardized. No location
is inferred from names, enumeration order, or coordinates. Other platforms
currently return unknown.

AudioRecorder.getDefaultInputDevice() returns an InputDevice with the same ID
as the device list, or null, without starting capture, requesting permission,
or changing routes:

| Platform | Meaning |
| --- | --- |
| Android | Live routed input while this recorder captures with automatic device selection |
| iOS | Current session input while this recorder captures with automatic device selection |
| macOS | Core Audio default input device |
| Windows | Default capture endpoint for the communications role, matching Media Foundation capture |
| Other/unsupported | null |

On Android and iOS, inactive capture, paused native recording, explicit device
selection, or an unidentifiable route produces null. The result is not a
prediction of the device used by a future recording. Consumers should query
again after lifecycle/device changes and discard stale results.

A default badge is informational. Selecting that device by ID fixes selection
to that device; it does not enable automatic routing. Existing null device
configuration continues to request automatic routing.
