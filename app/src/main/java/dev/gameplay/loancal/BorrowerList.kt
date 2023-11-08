package dev.gameplay.loancal

import android.os.Bundle
import android.provider.Settings
import android.content.Context
import android.widget.TextView
import android.app.Dialog
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dev.gameplay.loancal.adapter.LoanAdapter
import dev.gameplay.loancal.databinding.ActivityBorrowerListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import dev.gameplay.loancal.Class.LoanInfo
import dev.gameplay.loancal.Class.PaymentInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BorrowerList : AppCompatActivity() {
    lateinit var binding: ActivityBorrowerListBinding
    private val databaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBorrowerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val userDatabaseReference = databaseReference.child("loans").child(androidId)

        val borrowerList: MutableList<String> = mutableListOf()

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                borrowerList.clear()

                for (childSnapshot in dataSnapshot.children) {
                    if (childSnapshot.hasChild("borrowerName")) {
                        val borrowerName = childSnapshot.child("borrowerName").getValue(String::class.java)
                        if (!borrowerName.isNullOrBlank()) {
                            borrowerList.add(borrowerName)
                        }
                    }
                }

                val recyclerView = binding.recyclerView
                recyclerView.layoutManager = LinearLayoutManager(this@BorrowerList)

                val adapter = LoanAdapter(borrowerList) { borrowerName ->
                    showDialogWithBorrowerData(borrowerName)
                }
                recyclerView.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@BorrowerList, "Database error: " + databaseError.message, Toast.LENGTH_SHORT).show()
            }
        }

        userDatabaseReference.addValueEventListener(valueEventListener)
    }

    private fun showDialogWithBorrowerData(borrowerName: String) {
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val borrowerRef = databaseReference.child("loans").child(androidId).child(borrowerName)

        borrowerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val borrowerData = dataSnapshot.getValue(LoanInfo::class.java)

                if (borrowerData != null) {
                    val interestRate = borrowerData.interestRatePerMonth
                    val loanAmount = borrowerData.loanAmount.toString()
                    val loanTerm = borrowerData.loanTerm
                    val monthlyPayment = borrowerData.monthlyPayment.toString()
                    val totalPrincipal = borrowerData.totalPrincipalWithInterest.toString()
                    val totalLoanPayment = borrowerData.totalLoanPayment.toString()
                    val startDate = borrowerData.startDate // Get the start date from your Firebase data
                    val endDate = borrowerData.endDate // Get the end date from your Firebase data

                    showBorrowerDataDialog(
                        this@BorrowerList,
                        borrowerName,
                        interestRate,
                        loanAmount,
                        loanTerm,
                        monthlyPayment,
                        totalPrincipal,
                        totalLoanPayment,
                        startDate,
                        endDate
                    )
                } else {
                    Toast.makeText(this@BorrowerList, "Borrower data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@BorrowerList, "Database error: " + databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showBorrowerDataDialog(
        context: Context,
        borrowerName: String,
        interestRate: String,
        loanAmount: String,
        loanTerm: String,
        monthlyPayment: String,
        totalPrincipal: String,
        totalLoanPayment: String, // Corrected parameter here
        startDate: Date,
        endDate: Date
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.borrower_data)

        val borrowerNameTextView = dialog.findViewById<TextView>(R.id.borrowerNameTextView)
        val interestRateTextView = dialog.findViewById<TextView>(R.id.interestRateTextView)
        val loanAmountTextView = dialog.findViewById<TextView>(R.id.loanAmountTextView)
        val loanTermTextView = dialog.findViewById<TextView>(R.id.loanTermTextView)
        val monthlyPaymentTextView = dialog.findViewById<TextView>(R.id.monthlyPaymentTextView)
        val totalPrincipalTextView = dialog.findViewById<TextView>(R.id.totalPrincipalTextView)
        val totalLoanPaymentTextView = dialog.findViewById<TextView>(R.id.paymentRecord) // Corrected TextView name here
        val startDateTextView = dialog.findViewById<TextView>(R.id.startDate)
        val endDateTextView = dialog.findViewById<TextView>(R.id.endDate)
        val payment = dialog.findViewById<Button>(R.id.payment)

        val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        val formattedStartDate = dateFormat.format(endDate)
        val formattedEndDate = dateFormat.format(startDate)

        borrowerNameTextView.text = "Borrower Name: $borrowerName"
        interestRateTextView.text = "Interest Rate: $interestRate"
        loanAmountTextView.text = "Loan Amount: $loanAmount"
        loanTermTextView.text = "Loan Term: $loanTerm"
        monthlyPaymentTextView.text = "Monthly Payment: $monthlyPayment"
        totalPrincipalTextView.text = "Total Principal with Interest: $totalPrincipal"
        totalLoanPaymentTextView.text = "Total Loan Payment: $totalLoanPayment" // Corrected text here
        startDateTextView.text = "Start Date: $formattedStartDate"
        endDateTextView.text = "End Date: $formattedEndDate"

        payment.setOnClickListener {
            openPaymentDialog(borrowerName)

            dialog.dismiss()

        }

        dialog.setCancelable(true)
        dialog.show()
    }


    // Inside your BorrowerList class

    // Add this function to open the payment dialog
    private fun openPaymentDialog(borrowerName: String) {
        val paymentDialog = Dialog(this)
        paymentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        paymentDialog.setContentView(R.layout.payment_dialog) // Create a layout for payment input

        val paymentAmountEditText = paymentDialog.findViewById<EditText>(R.id.paymentAmountEditText)
        val confirmPaymentButton = paymentDialog.findViewById<Button>(R.id.confirmPaymentButton)

        confirmPaymentButton.setOnClickListener {
            val paymentAmountText = paymentAmountEditText.text.toString()
            if (paymentAmountText.isNotBlank()) {
                val paymentAmount = paymentAmountText.toDouble()
                recordUserPayment(borrowerName, paymentAmount)
                paymentDialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a valid payment amount.", Toast.LENGTH_SHORT).show()
            }
        }

        paymentDialog.setCancelable(true)
        paymentDialog.show()
    }

    // Create this function to record the user's payment in Firebase
    private fun recordUserPayment(borrowerName: String, paymentAmount: Double) {
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val borrowerRef = databaseReference.child("loans").child(androidId).child(borrowerName)

        borrowerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val borrowerData = dataSnapshot.getValue(LoanInfo::class.java)

                if (borrowerData != null) {
                    val totalLoanPayment = borrowerData.totalLoanPayment + paymentAmount

                    // Update the Firebase data with the new total payment
                    borrowerRef.child("totalLoanPayment").setValue(totalLoanPayment)
                        .addOnSuccessListener {
                            // Payment successfully recorded
                            Toast.makeText(this@BorrowerList, "Payment successfully recorded.", Toast.LENGTH_SHORT).show()

                            // Now, record the payment in the payment history
                            recordPaymentInHistory(androidId, borrowerName, paymentAmount)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@BorrowerList, "Failed to record the payment. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@BorrowerList, "Database error: " + databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun recordPaymentInHistory(userId: String, borrowerName: String, paymentAmount: Double) {
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())

        val paymentDate = dateFormat.format(Date()) // Get the current date in the desired format
        val paymentHistoryRef = databaseReference.child("payment_history").child(androidId).push()

        val paymentData = PaymentInfo(borrowerName, paymentAmount, paymentDate)

        // Set the payment history data under the user's ID
        paymentHistoryRef.setValue(paymentData)
            .addOnSuccessListener {
                // Payment history recorded successfully
                Toast.makeText(this@BorrowerList, "Payment history recorded.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this@BorrowerList, "Failed to record payment history. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }



}
