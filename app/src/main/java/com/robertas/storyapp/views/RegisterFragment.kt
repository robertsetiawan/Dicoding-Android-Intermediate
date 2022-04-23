package com.robertas.storyapp.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.robertas.storyapp.R
import com.robertas.storyapp.databinding.FragmentRegisterBinding
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.viewmodels.UserViewModel
import com.robertas.storyapp.views.components.CustomEmailEditText
import com.robertas.storyapp.views.components.CustomNameEditText
import com.robertas.storyapp.views.components.CustomPasswordEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class RegisterFragment : Fragment(), View.OnClickListener {

    private val loginViewModel by viewModels<UserViewModel>()

    private var _binding: FragmentRegisterBinding? = null

    private val binding get() = _binding

    private lateinit var navController: NavController

    private var registerButton: Button? = null

    private var loadingProgressBar: ProgressBar? = null

    private var emailEditText: CustomEmailEditText? = null

    private var nameEditText: CustomNameEditText? = null

    private var passwordEditText: CustomPasswordEditText? = null

    private var registerJob: Job = Job()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        bindViewToFragment()
    }


    private fun bindViewToFragment() {
        registerButton = binding?.registerBtn

        registerButton?.setOnClickListener(this)

        loadingProgressBar = binding?.progressLoading

        emailEditText = binding?.emailEt

        nameEditText = binding?.nameEt

        passwordEditText = binding?.passwordEt
    }

    private fun isEntryValid(): Boolean {
        val isEmailValid = emailEditText?.isInputValid ?: false

        val isNameValid = nameEditText?.isInputValid ?: false

        val isPasswordValid = passwordEditText?.isInputValid ?: false

        return (isEmailValid && isNameValid && isPasswordValid)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard()

        _binding = null

        registerButton = null

        loadingProgressBar = null

        nameEditText = null

        passwordEditText = null

        emailEditText = null

        if (registerJob.isActive) registerJob.cancel()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.register_btn -> {

                hideKeyBoard()

                if (isEntryValid()) {

                    processRegister(
                        nameEditText?.text.toString(),
                        emailEditText?.text.toString(),
                        passwordEditText?.text.toString()
                    )

                } else {
                    showErrorInput()
                }
            }

            else -> return
        }
    }

    private fun showErrorInput() {
        val isEmailValid = emailEditText?.isInputValid ?: false

        val isNameValid = nameEditText?.isInputValid ?: false

        val isPasswordValid = passwordEditText?.isInputValid ?: false

        if (!isEmailValid) {
            emailEditText?.showErrorMessage()
        }

        if (!isNameValid) {
            nameEditText?.showErrorMessage()
        }

        if (!isPasswordValid) {
            passwordEditText?.showErrorMessage()
        }
    }

    private fun setLoadingAndButtonVisibility(isEnable: Boolean) {
        binding?.apply {
            registerBtn.isVisible = isEnable

            progressLoading.isVisible = isVisible
        }
    }

    private fun showSnackBar(message: String) {
        binding?.root?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun processRegister(name: String, email: String, password: String) {

        lifecycleScope.launchWhenResumed {

            if (registerJob.isActive) registerJob.cancel()

            loginViewModel.register(name, email, password)
                .collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            setLoadingAndButtonVisibility(false)
                        }

                        is NetworkResult.Success -> {

                            setLoadingAndButtonVisibility(true)

                            showSnackBar(getString(R.string.success_registration))

                            val actionToLoginFragment =
                                RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()

                            navController.navigate(actionToLoginFragment)
                        }

                        is NetworkResult.Error -> {
                            setLoadingAndButtonVisibility(true)

                            showSnackBar(result.message)
                        }
                    }
                }
        }
    }

    private fun hideKeyBoard() {
        if (requireActivity().currentFocus == null) {
            return
        }
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
    }
}