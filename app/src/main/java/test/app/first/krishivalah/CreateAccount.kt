package test.app.first.krishivalah

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import test.app.first.krishivalah.databinding.FragmentCreateAccountBinding
import java.util.concurrent.TimeUnit


class CreateAccount : Fragment() {
    // TODO: Rename and change types of parameter
    private lateinit var binding: FragmentCreateAccountBinding
    private lateinit var phoneNumber:String
    private lateinit var name:String
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         binding= FragmentCreateAccountBinding.inflate(inflater,container,false);
         val view=binding.root
        //Initializing Firebase Auth
        auth= FirebaseAuth.getInstance();
        // Setting function of create button
        binding.createAccountButton.setOnClickListener(View.OnClickListener {
          phoneNumber=binding.phoneNumberTextInputEditText.text.toString().trim();
          name=binding.nameTextInputEditText.text.toString().trim()

            if(phoneNumber.length!=10){
                binding.phoneNumberTextInputLayout.error.contentEquals("Enter 10 digit number.")
            }else if(name.isEmpty()|| name.length<3){
                binding.nameTextInputLayout.error.contentEquals("Please enter correct name")
            }else{
                createCallbackVariable()
                phoneNumber= "+91$phoneNumber"
                val options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this.requireActivity())                 // Activity (for callback binding)
                        .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)


            }
        }
        )

        binding.createAccountLoginButton.setOnClickListener {
            val fragmentManager= activity?.supportFragmentManager;
            fragmentManager?.commit {
                replace<LogInFragment>(R.id.nav_host_fragment)
            }
        }
        return view
    }

    //Method to create callback variable
    private fun createCallbackVariable(){
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(context,"Enter number in correct format.",Toast.LENGTH_SHORT).show();
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(context,"Try after today.",Toast.LENGTH_SHORT).show();
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")
                binding.nameTextInputLayout.visibility=View.GONE
                binding.phoneNumberTextInputLayout.visibility=View.GONE
                binding.createAccountLoginButton.visibility=View.GONE
                binding.createAccountButton.visibility=View.GONE
                binding.otpPinView.visibility=View.VISIBLE
                binding.submitButton.visibility=View.VISIBLE
                binding.otpLottie.visibility=View.VISIBLE
                val credential = PhoneAuthProvider.getCredential(verificationId!!, token.toString())

                signInWithPhoneAuthCredential(credential)
            }
        }

    }

    //After completion of verification of phone number and verifying otp
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        activity?.let {
            auth.signInWithCredential(credential)
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")

                        val user = task.result?.user
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                        // Update UI
                    }
                }
        }
    }

}

