import AVFoundation

/// Serializes non-interleaved native Float32 buffers as interleaved
/// little-endian IEEE 754 PCM.
class PcmFloat32Encoder: AudioEnc {
  func setup(config: RecordConfig, format: AVAudioFormat) throws {}

  func encode(buffer: AVAudioPCMBuffer) -> [Data] {
    guard let channelData = buffer.floatChannelData else { return [] }

    let frameCount = Int(buffer.frameLength)
    let channels = Int(buffer.format.channelCount)
    var bytes = Data(capacity: frameCount * channels * MemoryLayout<Float>.size)

    for frame in 0..<frameCount {
      for channel in 0..<channels {
        var bits = channelData[channel][frame].bitPattern.littleEndian
        withUnsafeBytes(of: &bits) { bytes.append(contentsOf: $0) }
      }
    }
    return [bytes]
  }

  func dispose() {}
}
