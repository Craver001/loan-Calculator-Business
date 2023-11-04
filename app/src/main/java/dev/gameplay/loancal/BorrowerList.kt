package dev.gameplay.loancal

import android.os.Bundle
import android.provider.Settings
import android.content.Context
import android.widget.TextView
import android.app.Dialog
import android.view.Window
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

                    showBorrowerDataDialog(
                        this@BorrowerList,
                        borrowerName,
                        interestRate,
                        loanAmount,
                        loanTerm,
                        monthlyPayment,
                        totalPrincipal
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
        totalPrincipal: String
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

        borrowerNameTextView.text = "Borrower Name: $borrowerName"
        interestRateTextView.text = "Interest Rate: $interestRate"
        loanAmountTextView.text = "Loan Amount: $loanAmount"
        loanTermTextView.text = "Loan Term: $loanTerm"
        monthlyPaymentTextView.text = "Monthly Payment: $monthlyPayment"
        totalPrincipalTextView.text = "Total Principal with Interest: $totalPrincipal"

        dialog.setCancelable(true)
        dialog.show()
    }
}
