package com.robertas.storyapp.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.robertas.storyapp.R
import com.robertas.storyapp.databinding.FragmentRegisterBinding
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.viewmodels.RegisterViewModel
import com.robertas.storyapp.views.components.CustomEmailEditText
import com.robertas.storyapp.views.components.CustomNameEditText
import com.robertas.storyapp.views.components.CustomPasswordEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment(), View.OnClickListener {

    private val registerViewModel by viewModels<RegisterViewModel>()

    private var _binding: FragmentRegisterBinding? = null

    private val binding get() = _binding

    private lateinit var navController: NavController

    private var registerButton: Button ?= null

    private var loadingProgressBar: ProgressBar ?= null

    private var emailEditText: CustomEmailEditText ?= null

    private var nameEditText: CustomNameEditText ?= null

    private var passwordEditText: CustomPasswordEditText ?= null

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

        registerButton?.setOnClickListener(this)

        setupRegisterObserver()
    }

    private fun setupRegisterObserver() {
        val registerObserver = Observer<NetworkResult<Boolean?>>{ result ->
            when (result) {
                is NetworkResult.Loading -> {}

                is NetworkResult.Success -> {

                    binding?.apply {
                        registerBtn.visibility = View.VISIBLE

                        progressLoading.visibility = View.GONE
                    }

                    binding?.root?.let { it -> Snackbar.make(it, getString(R.string.success_registration), Snackbar.LENGTH_SHORT).show() }

                    val actionToLoginFragment = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()

                    navController.navigate(actionToLoginFragment)

                    registerViewModel.doneNavigating()
                }

                is NetworkResult.Error -> {
                    binding?.apply {
                        registerBtn.visibility = View.VISIBLE

                        progressLoading.visibility = View.GONE
                    }

                    binding?.root?.let { it -> Snackbar.make(it, result.message, Snackbar.LENGTH_SHORT).show() }

                    registerViewModel.doneNavigating()
                }
            }
        }

        registerViewModel.registerState.observe(viewLifecycleOwner, registerObserver)
    }


    private fun bindViewToFragment(){
        registerButton = binding?.registerBtn

        loadingProgressBar = binding?.progressLoading

        emailEditText = binding?.emailEt

        nameEditText = binding?.nameEt

        passwordEditText = binding?.passwordEt
    }

    private fun isEntryValid(): Boolean{
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
    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.register_btn -> {

                hideKeyBoard()

                if (isEntryValid()){

                    binding?.apply {
                        registerBtn.visibility = View.GONE

                        progressLoading.visibility = View.VISIBLE
                    }

                    registerViewModel.register(nameEditText?.text.toString(), emailEditText?.text.toString(), passwordEditText?.text.toString())

                } else {
                    val isEmailValid = emailEditText?.isInputValid ?: false

                    val isNameValid = nameEditText?.isInputValid ?: false

                    val isPasswordValid = passwordEditText?.isInputValid ?: false

                    if (!isEmailValid){
                        emailEditText?.showErrorMessage()
                    }

                    if (!isNameValid) {
                        nameEditText?.showErrorMessage()
                    }

                    if (!isPasswordValid){
                        passwordEditText?.showErrorMessage()
                    }
                }
            }

            else -> return
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