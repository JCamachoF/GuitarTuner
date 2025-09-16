package com.daza.guitartuner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor


class TunerFragment : Fragment() {

    private lateinit var noteText: TextView
    private lateinit var noteStrip: NoteStripView
    private lateinit var tuneStatus: TextView
    private var dispatcher: AudioDispatcher? = null

    val MIN_RMS_THRESHOLD = 0.02f  // Ajusta según pruebas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tuner, container, false)

        noteText = view.findViewById(R.id.noteText)
        noteStrip = view.findViewById(R.id.noteStrip)
        tuneStatus = view.findViewById(R.id.tuneStatus)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            startTuner()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        return view
    }
    private fun startTuner() {
        val sampleRate = 22050
        val bufferSize = 1024
        val overlap = 0

       dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, overlap)
        val pdh = PitchDetectionHandler { res, _ ->
            val pitchInHz = res.pitch
            requireActivity().runOnUiThread {
                if (pitchInHz > 0) {
                    val noteNumber = 12 * (Math.log10(pitchInHz / 440.0) / Math.log10(2.0)) + 69
                    val rounded = Math.round(noteNumber).toInt()
                    val note = getNoteFromPitch(pitchInHz)

                    // Aquí hacemos el cálculo para posicionar la línea roja
                    val totalSteps = 12 * 12  // 144 pasos (12 notas * 12 semitonos)

                    val fractionalPosition = (noteNumber % 12).toFloat()
                    noteText.text = note

                    noteStrip.updatePosition(fractionalPosition)


                    val noteFrequency = 440.0 * Math.pow(2.0, (rounded - 69) / 12.0)
                    val difference = pitchInHz - noteFrequency

                    val statusText = when {
                        kotlin.math.abs(difference) < 1 -> "Afinado"
                        difference > 0 -> "Un poco alto"
                        else -> "Un poco bajo"
                    }

                    tuneStatus.text = statusText
                    tuneStatus.setTextColor(
                        when (statusText) {
                            "Afinado" -> Color.GREEN
                            "Ajustar" -> Color.YELLOW
                            else -> Color.RED
                        }
                    )
                } else {
                    noteText.text = "Sin señal"
                    noteText.textSize = 20f  // tamaño en sp
                    tuneStatus.text = ""
                }
            }
        }

        val volumeFilter = object : AudioProcessor {
            override fun process(audioEvent: AudioEvent): Boolean {
                val rms = audioEvent.rms
                return rms > MIN_RMS_THRESHOLD // solo deja pasar si hay suficiente volumen
            }

            override fun processingFinished() {}
        }

        dispatcher?.addAudioProcessor(volumeFilter)

         val pitchProcessor = PitchProcessor(
           PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
           sampleRate.toFloat(),
           bufferSize,
           pdh
       )

       dispatcher?.addAudioProcessor(pitchProcessor)
        Thread(dispatcher, "Audio Dispatcher").start()
    }

    private fun getNoteFromPitch(pitch: Float): String {
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val noteNumber = 12 * (Math.log10(pitch / 440.0) / Math.log10(2.0)) + 69
        val rounded = Math.round(noteNumber).toInt()

        val note = noteNames[rounded % 12]
        val octave = (rounded / 12) - 1
        return "$note$octave"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTuner()
            } else {
                // Mostrar mensaje o manejar rechazo de permiso
                noteText.text = "Permiso de micrófono denegado"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dispatcher?.stop()
        dispatcher = null
    }


}