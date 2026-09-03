#pragma once

#include <string>
#include <flutter/encodable_value.h>

namespace record_windows
{

	struct AudioEncoder
	{
		static constexpr const char* aacLc     = "aacLc";
		static constexpr const char* aacEld    = "aacEld";
		static constexpr const char* aacHe     = "aacHe";
		static constexpr const char* amrNb     = "amrNb";
		static constexpr const char* amrWb     = "amrWb";
		static constexpr const char* opus      = "opus";
		static constexpr const char* flac      = "flac";
		static constexpr const char* pcm16bits = "pcm16bits";
		static constexpr const char* wav       = "wav";
	};

	struct PcmFormat
	{
		static constexpr const char* int16   = "int16";
		static constexpr const char* float32 = "float32";
	};

	struct RecordConfig
	{
		std::string encoderName = AudioEncoder::aacLc;
		std::string deviceId = {};
		int bitRate = 128000;
		int sampleRate = 44100;
		int numChannels = 2;
		std::string pcmFormat = PcmFormat::int16;
		bool autoGain = false;
		bool echoCancel = false;
		bool noiseSuppress = false;
		flutter::EncodableMap rawArgs;

		RecordConfig(
			const std::string& encoderName,
			const std::string& deviceId,
			int bitRate,
			int sampleRate,
			int numChannels,
			const std::string& pcmFormat,
			bool autoGain,
			bool echoCancel,
			bool noiseSuppress,
			flutter::EncodableMap rawArgs)
			: encoderName(encoderName),
			deviceId(deviceId),
			bitRate(bitRate),
			sampleRate(sampleRate),
			numChannels(numChannels),
			pcmFormat(pcmFormat == PcmFormat::float32 ? PcmFormat::float32 : PcmFormat::int16),
			autoGain(autoGain),
			echoCancel(echoCancel),
			noiseSuppress(noiseSuppress),
			rawArgs(std::move(rawArgs))
		{
		}

		bool usesFloatPcm() const
		{
			return pcmFormat == PcmFormat::float32 &&
				(encoderName == AudioEncoder::pcm16bits || encoderName == AudioEncoder::wav);
		}
	};
};
