package com.daza.guitartuner

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ReferenceNotesFragment : Fragment() {

    private val notes = listOf("E2", "A2", "D3", "G3", "B3", "E4")
    private val soundMap = mutableMapOf<String, MediaPlayer>()
    private var currentPlayer: MediaPlayer? = null  // ← NUEVA VARIABLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reference_notes, container, false)

        val stringViews = listOf(
            view.findViewById<View>(R.id.stringE2),
            view.findViewById<View>(R.id.stringA2),
            view.findViewById<View>(R.id.stringD3),
            view.findViewById<View>(R.id.stringG3),
            view.findViewById<View>(R.id.stringB3),
            view.findViewById<View>(R.id.stringE4)
        )

        notes.forEachIndexed { index, note ->
            val resId = resources.getIdentifier(note.lowercase(), "raw", requireContext().packageName)
            if (resId != 0) {
                val player = MediaPlayer.create(requireContext(), resId)
                player.isLooping = false // opcional
                soundMap[note] = player

                stringViews[index].setOnClickListener {
                    currentPlayer?.let {
                        if (it.isPlaying) {
                            it.pause()
                            it.seekTo(0)
                        }
                    }
                    currentPlayer = player
                    player.seekTo(0)
                    player.start()
                }
            }
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        soundMap.values.forEach { it.release() }
    }
}
