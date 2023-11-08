package dev.gameplay.loancal
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.gameplay.loancal.databinding.PolicyBinding

class PolicyConfirmation : AppCompatActivity() {
    lateinit var binding: PolicyBinding

    // Define a SharedPreferences object to store user preferences
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences with a unique name for your app
        sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

        // Check if the user has already accepted the policy
        val policyAccepted = sharedPreferences.getBoolean("PrivacyPolicyAccepted", false)

        if (policyAccepted) {
            // The policy has already been accepted, open MainActivity directly
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Load your policy URL from Firebase Hosting
            val policyUrl = "https://full-version-7aeba.web.app/"
            binding.webViewPrivacyPolicy.loadUrl(policyUrl)

            // Configure WebView settings
            val webSettings: WebSettings = binding.webViewPrivacyPolicy.settings
            webSettings.javaScriptEnabled = true // Enable JavaScript if needed

            // Set a WebViewClient to handle WebView events, including page loading progress
            binding.webViewPrivacyPolicy.webViewClient = object : WebViewClient() {
                // Override onPageFinished to hide the progress bar when the page is loaded
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Hide the progress bar
                    binding.progressBar.visibility = View.GONE
                }

                // You can also override other methods like onPageStarted or onReceivedError if needed
            }

            binding.acceptButton.setOnClickListener {
                // Check if both consent and data policy checkboxes are checked
                val consentChecked = binding.consent.isChecked
                val dataPolicyChecked = binding.dataCollection.isChecked

                if (consentChecked && dataPolicyChecked) {
                    // User accepted the policy
                    sharedPreferences.edit().putBoolean("PrivacyPolicyAccepted", true).apply()

                    // Proceed to the main activity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Inform the user to check both checkboxes
                    Toast.makeText(this, "Please check both data collection and consent to accept this policy",
                        Toast.LENGTH_SHORT).show()
                }
            }

            binding.rejectButton.setOnClickListener {
                // Close the application
                finishAffinity()
            }
        }
    }
}
