package dev.gameplay.loancal

import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import dev.gameplay.loancal.Class.PaymentInfo
import dev.gameplay.loancal.adapter.PaymentHistoryAdapter
import dev.gameplay.loancal.databinding.ActivityPaymentHistoryBinding

class PaymentHistory : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentHistoryBinding
    private lateinit var paymentHistoryAdapter: PaymentHistoryAdapter
    private val paymentHistoryList: MutableList<PaymentInfo> = mutableListOf() // Initialize with your payment history data

    // Initialize Firebase Database reference
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this@PaymentHistory)

        // Query your payment history data from Firebase
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val paymentHistoryRef = databaseReference.child("payment_history").child(androidId)

        paymentHistoryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newPaymentHistoryList = mutableListOf<PaymentInfo>() // Create a new list

                for (childSnapshot in dataSnapshot.children) {
                    val paymentInfo = childSnapshot.getValue(PaymentInfo::class.java)
                    paymentInfo?.let { newPaymentHistoryList.add(it) }
                }

                paymentHistoryList.clear() // Clear the old list
                paymentHistoryList.addAll(newPaymentHistoryList) // Add all elements from the new list

                paymentHistoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })

        // Provide the onItemClick callback when creating the adapter
        paymentHistoryAdapter = PaymentHistoryAdapter(paymentHistoryList) { borrowerName ->
            // Handle item click here
            // You can navigate to a detailed view or perform any other action
        }

        recyclerView.adapter = paymentHistoryAdapter
    }
}
