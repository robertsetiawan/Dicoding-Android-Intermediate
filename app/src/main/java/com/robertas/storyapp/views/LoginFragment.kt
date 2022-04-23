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
import com.robertas.storyapp.databinding.FragmentLoginBinding
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment(), View.OnClickListener {

    private val userViewModel by viewModels<UserViewModel>()

    private lateinit var navController: NavController

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding

    private var loginButton: Button? = null

    private var loadingProgressBar: ProgressBar? = null

    private var loginJob: Job = Job()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        loginButton = binding?.loginBtn

        loadingProgressBar = binding?.progressLoading

        binding?.registerBtn?.setOnClickListener(this)

        binding?.loginBtn?.setOnClickListener(this)

        setupLoginObserver()
    }

    private fun setupLoginObserver() {

        if (userViewModel.isUserLoggedIn()) {
            val actionToHomeFragment = LoginFragmentDirections.actionLoginFragmentToHomeFragment()

            navController.navigate(actionToHomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard()

        _binding = null

        loginButton = null

        loadingProgressBar = null

        if (loginJob.isActive) loginJob.cancel()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.register_btn -> navController.navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())

            R.id.login_btn -> {
                hideKeyBoard()

                processLogin(
                    binding?.emailEt?.text.toString().trim(),
                    binding?.passwordEt?.text.toString()
                )
            }

            else -> return
        }
    }

    private fun setButtonVisibility(isVisible: Boolean) {
        binding?.apply {
            registerBtn.isEnabled = isVisible

            loginBtn.isVisible = isVisible
        }
    }

    private fun setLoadingStatus(isVisible: Boolean) {
        loadingProgressBar?.isVisible = isVisible
    }

    private fun showSnackBar(message: String) {
        binding?.root?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun processLogin(email: String, password: String) {

        lifecycleScope.launchWhenResumed {

            if (loginJob.isActive) loginJob.cancel()

            loginJob = launch {

                userViewModel.login(email, password)
                    .collect { result ->
                        when (result) {
                            is NetworkResult.Loading -> {
                                setButtonVisibility(false)

                                setLoadingStatus(true)
                            }

                            is NetworkResult.Error -> {

                                showSnackBar(result.message)

                                setButtonVisibility(true)

                                setLoadingStatus(false)
                            }

                            is NetworkResult.Success -> {

                                setButtonVisibility(true)

                                setLoadingStatus(false)

                                showSnackBar(getString(R.string.welcome, result.data?.name))

                                val actionToHomeFragment =
                                    LoginFragmentDirections.actionLoginFragmentToHomeFragment()

                                navController.navigate(actionToHomeFragment)
                            }
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