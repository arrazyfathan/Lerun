package com.androiddevs.lerun.presentation.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.androiddevs.lerun.R
import com.androiddevs.lerun.databinding.FragmentSettingsBinding
import com.androiddevs.lerun.presentation.home.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top + 16
            }

            WindowInsetsCompat.CONSUMED
        }

        binding.btnEditProfile.setOnClickListener {
            showDialogEditProfile()
        }
    }

    private fun showDialogEditProfile() {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_edit_profile, null)
        dialog.setContentView(view)

        val name = view.findViewById<EditText>(R.id.et_profile_name)
        val weight = view.findViewById<EditText>(R.id.et_profile_weight)
        val validation = view.findViewById<TextView>(R.id.validation_profile)
        val btnApply = view.findViewById<MaterialButton>(R.id.btn_save_dialog_profile)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btn_save_cancel_profile)

        // set current data
        val nameSharedPref = "Default Name"
        val weightSharedPref = "Default Weight"

        name.setText(nameSharedPref)
        weight.setText(weightSharedPref.toString())

        name.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int,
                ) {
                    if (p0.isNullOrEmpty() || p0.isBlank()) {
                        validation.visibility = View.VISIBLE
                    }
                }

                override fun onTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int,
                ) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    validation.visibility = View.GONE
                }
            },
        )

        weight.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int,
                ) {
                    if (p0.isNullOrEmpty() || p0.isBlank()) {
                        validation.visibility = View.VISIBLE
                    }
                }

                override fun onTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int,
                ) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    validation.visibility = View.GONE
                }
            },
        )

        btnApply.setOnClickListener {
            /*if (name.text.isBlank() || name.text.isNullOrEmpty() || weight.text.isBlank() || weight.text.isNullOrEmpty()) {
                validation.visibility = View.VISIBLE
            } else {
                val nameText = name.text.toString()
                val weightText = weight.text.toString()

                sharedPref.edit()
                    .putString(KEY_NAME, nameText)
                    .putFloat(KEY_WEIGHT, weightText.toFloat())
                    .apply()

                Snackbar.make(
                    view,
                    "Saved",
                    Snackbar.LENGTH_LONG,
                ).show()

                dialog.dismiss()
            }*/
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
