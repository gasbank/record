#include "amplitude/amplitude_tracker.h"

#include <cmath>
#include <cstdint>
#include <vector>

namespace record_windows {

void AmplitudeTracker::update(const BYTE* chunk, DWORD size, bool isFloat) {
	double maxSample = 0.0;
	if (isFloat) {
		std::vector<float> samples(size / sizeof(float));
		CopyMemory(samples.data(), chunk, samples.size() * sizeof(float));
		for (auto sample : samples) {
			if (!std::isfinite(sample)) continue;
			double value = std::abs(static_cast<double>(sample));
			if (value > maxSample) maxSample = value;
		}
	} else {
		std::vector<int16_t> samples(size / sizeof(int16_t));
		CopyMemory(samples.data(), chunk, samples.size() * sizeof(int16_t));
		for (auto sample : samples) {
			double value = std::abs(static_cast<int>(sample)) / 32767.0;
			if (value > maxSample) maxSample = value;
		}
	}

	const double decibels = maxSample > 0.0 ? 20.0 * std::log10(maxSample) : -160.0;
	current = decibels < 0.0 ? decibels : 0.0;
	if (current > peak) peak = current;
}

void AmplitudeTracker::reset() {
	current = -160.0;
	peak    = -160.0;
}

} // namespace record_windows
