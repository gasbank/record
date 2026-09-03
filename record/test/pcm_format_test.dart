import 'package:flutter_test/flutter_test.dart';
import 'package:record/record.dart';

void main() {
  group('PcmFormat', () {
    test('defaults to signed 16-bit PCM', () {
      const config = RecordConfig();

      expect(config.pcmFormat, PcmFormat.int16);
      expect(config.toMap()['pcmFormat'], 'int16');
    });

    test('round-trips Float32 through the platform map', () {
      const config = RecordConfig(pcmFormat: PcmFormat.float32);

      final restored = RecordConfig.fromMap(config.toMap());

      expect(restored.pcmFormat, PcmFormat.float32);
      expect(config.copyWith().pcmFormat, PcmFormat.float32);
    });

    test('uses the compatible default for missing or unknown values', () {
      expect(RecordConfig.fromMap(const {}).pcmFormat, PcmFormat.int16);
      expect(
        RecordConfig.fromMap(const {'pcmFormat': 'futureFormat'}).pcmFormat,
        PcmFormat.int16,
      );
    });
  });
}
