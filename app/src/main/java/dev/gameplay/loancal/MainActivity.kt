package dev.gameplay.loancal

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase
import dev.gameplay.loancal.Class.LoanInfo
import dev.gameplay.loancal.databinding.ActivityMainBinding
import java.util.Calendar
import java.util.Date


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.calculator.setOnClickListener {
            calculatorDialog()
        }

        binding.generateLoan.setOnClickListener {
            genereteLoan()
        }

        binding.borrowerList.setOnClickListener {
            val intent = Intent(this@MainActivity,BorrowerList::class.java)
            startActivity(intent)
        }

    }

    fun calculatorDialog() {
        val dialogView = layoutInflater.inflate(R.layout.calculator_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Loan Calculator")

        val dialog = dialogBuilder.show()

        val calculateButton = dialogView.findViewById<Button>(R.id.calculateButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val amountEditText = dialogView.findViewById<EditText>(R.id.amountEditText)
        val interestRateEditText = dialogView.findViewById<EditText>(R.id.interestRateEditText)
        val termEditText = dialogView.findViewById<EditText>(R.id.termEditText)

        calculateButton.setOnClickListener {
            val amount = amountEditText.text.toString().toDouble()
            val interestRate = interestRateEditText.text.toString().toDouble() // Monthly interest rate
            val term = termEditText.text.toString().toInt()

            val monthlyInterestRate = interestRate / 100
            val numberOfPayments = term
            val monthlyPayment = (amount * monthlyInterestRate) / (1 - Math.pow(1 + monthlyInterestRate, -numberOfPayments.toDouble()))

            val totalInterest = (monthlyPayment * numberOfPayments) - amount

            dialog.dismiss()

            // Display the result dialog here
            showResultDialog(monthlyPayment, totalInterest, numberOfPayments)
        }


        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showResultDialog(monthlyPayment: Double, totalInterest: Double, numberOfPayments: Int) {
        val resultDialogView = LayoutInflater.from(this).inflate(R.layout.result_dialog, null)

        resultDialogView.findViewById<TextView>(R.id.monthlyPaymentTextView).text =
            "Monthly Payment: ${monthlyPayment}"
        resultDialogView.findViewById<TextView>(R.id.totalInterestTextView).text =
            "Total Interest: ${totalInterest}"
        resultDialogView.findViewById<TextView>(R.id.numberOfPaymentsTextView).text =
            "Number of Payments: ${numberOfPayments} months"

        val resultDialogBuilder = AlertDialog.Builder(this)
            .setView(resultDialogView)
            .setTitle("Loan Calculation Result")

        val resultDialog = resultDialogBuilder.show()
    }

    fun genereteLoan() {
        val dialogView = layoutInflater.inflate(R.layout.activity_generate_loan, null)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Loan Calculator")

        val dialog = dialogBuilder.create()
        dialog.show()

        val calculateButton = dialogView.findViewById<Button>(R.id.generateLoan)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel)
        val nameEditText = dialogView.findViewById<EditText>(R.id.editTextText)
        val amountEditText = dialogView.findViewById<EditText>(R.id.editTextNumber)
        val loanSpinner = dialogView.findViewById<Spinner>(R.id.loanSpinner)
        val interestSpinner = dialogView.findViewById<Spinner>(R.id.interestSpinner)

        // Set hint for the loan term Spinner
        val loanHint = "Select loan term (months)"
        val loanOptions = listOf(loanHint) + (1..12).map { "$it months" }
        val loanAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, loanOptions)
        loanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        loanSpinner.adapter = loanAdapter
        loanSpinner.setSelection(0, false)

        // Set hint for the interest Spinner
        val interestHint = "Interest per month (%)"
        val interestOptions = listOf(interestHint) + (3..20).map { "$it%" }
        val interestAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, interestOptions)
        interestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        interestSpinner.adapter = interestAdapter
        interestSpinner.setSelection(0, false)

        loanSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // The hint is selected, do nothing or display a message
                } else {
                    // A loan term is selected, you can get the value without " months" using:
                    val selectedLoanTerm = loanOptions[position].replace(" months", "")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when nothing is selected
            }
        }

        interestSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // The hint is selected, do nothing or display a message
                } else {
                    // An interest rate is selected, you can get the value without "%" using:
                    val selectedInterestRate = interestOptions[position].replace("%", "")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when nothing is selected
            }
        }

        calculateButton.setOnClickListener {
            if (isNetworkConnected(this)) {
                // Check for an active internet connection and proceed with data submission

                // Get the selected values from spinners
                val borrowerName = nameEditText.text.toString()
                val loanAmount = amountEditText.text.toString().toDouble()
                val loanTerm = loanSpinner.selectedItem.toString()
                val interestRate = interestSpinner.selectedItem.toString()

                // Parse loanTerm and interestRate to remove extra text (e.g., " months", "%")
                val parsedLoanTerm = loanTerm.split(" ")[0].toInt()
                val parsedInterestRate = interestRate.split("%")[0].toDouble() / 100.0

                // Calculate the monthly payment
                val monthlyInterestRate = parsedInterestRate
                val numberOfPayments = parsedLoanTerm
                val monthlyPayment = (loanAmount * monthlyInterestRate) / (1 - Math.pow(1 + monthlyInterestRate, -numberOfPayments.toDouble()))

                // Calculate the total principal with interest
                val totalPrincipalWithInterest = (monthlyPayment * numberOfPayments).toInt()

                val currentDate = Date() // Get the current date

                // Calculate the end date based on the start date (current date) and loan term
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                calendar.add(Calendar.MONTH, parsedLoanTerm)

                val endDate = calendar.time

                val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                val totalLoanPayment =0

                // Create a LoanInfo object with start and end dates
                val loanInfo = LoanInfo(
                    borrowerName,
                    loanAmount.toInt(),
                    loanTerm,
                    interestRate,
                    monthlyPayment.toInt(),
                    totalPrincipalWithInterest,
                    totalLoanPayment,
                    currentDate, // Start date
                    endDate // End date
                )

                // Upload the data to Firebase Realtime Database under the user's unique ID
                val databaseReference = FirebaseDatabase.getInstance().reference.child("loans").child(androidId).child(borrowerName)
                databaseReference.setValue(loanInfo)
                    .addOnSuccessListener {
                        // Data was successfully sent
                        showToast("Loan information submitted successfully.")
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        // There was an error sending the data
                        showToast("Failed to submit loan information. Please check your internet connection.")
                    }
            } else {
                // No internet connection
                showToast("No internet connection. Please check your network settings.")
            }
        }




        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

}
