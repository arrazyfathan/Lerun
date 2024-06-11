package com.androiddevs.lerun.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.androiddevs.lerun.BuildConfig
import com.androiddevs.lerun.R
import com.androiddevs.lerun.databinding.FragmentSettingsBinding
import com.androiddevs.lerun.presentation.home.MainViewModel
import com.androiddevs.lerun.utils.BitmapConverter
import com.androiddevs.lerun.utils.viewBinding
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding by viewBinding(FragmentSettingsBinding::bind)
    private val viewModel: MainViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()


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
        setupView()
        observe()
    }

    private fun observe() {
        settingViewModel.userImage().observe(viewLifecycleOwner) { userImage ->
            if (userImage != null) {
                if (!userImage.imageString.isNullOrBlank())
                    binding.tvProfile.setImageBitmap(
                        BitmapConverter.converterStringToBitmap(
                            userImage.imageString!!
                        )
                    )
                else
                    binding.tvProfile.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.profile_n
                        )
                    )
            } else {
                binding.tvProfile.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.profile_n
                    )
                )
            }
        }
    }

    private fun setupView() {
        binding.btnEditProfile.setOnClickListener {
            showDialogEditProfile()
        }

        binding.btnLicenses.setOnClickListener {
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
        }

        binding.versionInfo.text = getString(R.string.version_name, BuildConfig.VERSION_NAME)

        binding.tvProfile.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.profile_n
            )
        )
        binding.username.text = settingViewModel.getUsername()

        binding.tvProfile.setOnClickListener {
            // pickImage()
        }


        val theme = settingViewModel.getTheme()
        binding.darkThemeToggle.isChecked = theme != AppCompatDelegate.MODE_NIGHT_NO

        binding.darkThemeToggle.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            if (isChecked) {
                settingViewModel.setTheme(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                settingViewModel.setTheme(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun pickImage() {
        pickMedia.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly).build()
        )

    }

    private var pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val options = CropImageOptions().apply {
                guidelines = CropImageView.Guidelines.ON
            }
            cropImage.launch(
                CropImageContractOptions(
                    uri = uri,
                    options
                )
            )
        } else {
            Timber.tag("PhotoPicker").d("No media selected")
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val converted = BitmapConverter.convertImageUriToBase64(result.uriContent!!, requireContext())
            settingViewModel.changeImage(converted)
        } else {
            val exception = result.error
            Timber.e(exception)
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
        val btnChangeImage = view.findViewById<MaterialButton>(R.id.btn_change_image)
        val imageEdit = view.findViewById<ImageView>(R.id.image_edit)

        val nameSharedPref = viewModel.getProfileName()
        val weightSharedPref = viewModel.getProfileWeight()

        settingViewModel.userImage().observe(viewLifecycleOwner) { userImage ->
            if (userImage != null) {
                if (!userImage.imageString.isNullOrBlank())
                    imageEdit.setImageBitmap(
                        BitmapConverter.converterStringToBitmap(
                            userImage.imageString!!
                        )
                    )
                else
                    imageEdit.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.profile_n
                        )
                    )
            } else {
                imageEdit.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.profile_n
                    )
                )
            }
        }

        name.setText(nameSharedPref)
        weight.setText(weightSharedPref.toString())

        btnChangeImage.setOnClickListener {
            pickImage()
        }

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
            if (name.text.isBlank() || name.text.isNullOrEmpty() || weight.text.isBlank() || weight.text.isNullOrEmpty()) {
                validation.visibility = View.VISIBLE
            } else {
                val nameText = name.text.toString()
                val weightText = weight.text.toString()

                viewModel.setUserProfile(nameText, weightText)

                Snackbar.make(
                    view,
                    "Saved",
                    Snackbar.LENGTH_LONG,
                ).show()

                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
    }
}
