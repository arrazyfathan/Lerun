package com.arrazyfathan.lerun.presentation.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.arrazyfathan.lerun.feature.onboarding.R
import com.arrazyfathan.lerun.feature.onboarding.databinding.FragmentSetupBinding
import com.arrazyfathan.lerun.navigation.AppNavigator
import com.arrazyfathan.lerun.utils.viewBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {
    private val binding by viewBinding(FragmentSetupBinding::bind)
    private val viewModel: OnboardingViewModel by viewModels()
    private val navigator: AppNavigator
        get() = requireActivity() as AppNavigator

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isUserFilledUserProfile()) {
            navigator.openRun(clearOnboardingFromBackStack = true)
        }

        binding.tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success) {
                navigator.openRun(clearOnboardingFromBackStack = true)
            } else {
                Snackbar.make(requireView(), "Please enter all the fields", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if (name.isEmpty() || name.isBlank() || weight.isEmpty() || weight.isBlank()) {
            return false
        }

        viewModel.setUserProfile(name, weight)
        return true
    }
}
