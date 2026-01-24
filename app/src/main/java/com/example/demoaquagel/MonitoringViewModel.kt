package com.example.demoaquagel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.ArrayDeque
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MonitoringSample(
    val timestamp: Long,
    val temperature: Float,
    val humidity: Float,
    val impedance: Int
)

class MonitoringViewModel : ViewModel() {
    private val random = Random(System.currentTimeMillis())
    private val _latestSample = MutableStateFlow(generateSample())
    val latestSample: StateFlow<MonitoringSample> = _latestSample.asStateFlow()

    private val recentBuffer = ArrayDeque<MonitoringSample>()
    val recentSamples: List<MonitoringSample>
        get() = recentBuffer.toList()

    init {
        recentBuffer.addLast(_latestSample.value)
        viewModelScope.launch {
            while (true) {
                delay(random.nextLong(1000L, 2000L))
                val sample = generateSample()
                _latestSample.value = sample
                recentBuffer.addLast(sample)
                if (recentBuffer.size > 30) {
                    recentBuffer.removeFirst()
                }
            }
        }
    }

    private fun generateSample(): MonitoringSample {
        val temperature = random.nextDouble(36.0, 38.5).toFloat()
        val humidity = random.nextDouble(40.0, 85.0).toFloat()
        val impedance = random.nextInt(350, 901)
        return MonitoringSample(
            timestamp = System.currentTimeMillis(),
            temperature = temperature,
            humidity = humidity,
            impedance = impedance
        )
    }
}
