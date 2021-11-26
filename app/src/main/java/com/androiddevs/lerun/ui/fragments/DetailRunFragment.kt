package com.androiddevs.lerun.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.androiddevs.lerun.R
import com.androiddevs.lerun.databinding.FragmentDetailRunBinding

class DetailRunFragment : Fragment() {

    private var _binding: FragmentDetailRunBinding? = null
    private val binding get() = _binding!!

    val args: RunFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailRunBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val run = args.latestrun
        binding.tvRunTry.text = run.toString()


    }
}