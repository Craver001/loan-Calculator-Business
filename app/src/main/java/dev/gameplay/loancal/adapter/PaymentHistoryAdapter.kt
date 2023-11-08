package dev.gameplay.loancal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.gameplay.loancal.Class.PaymentInfo
import dev.gameplay.loancal.R

class PaymentHistoryAdapter(
    private val paymentInfoList: List<PaymentInfo>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_EMPTY = 0
    private val VIEW_TYPE_PAYMENT = 1

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val borrowerName: TextView = itemView.findViewById(R.id.borrowerNameTextView)
        val paymentAmount: TextView = itemView.findViewById(R.id.paymentAmountTextView)
        val paymentDate: TextView = itemView.findViewById(R.id.timestampTextView)
    }

    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.empty_borrower_layout, parent, false)
                EmptyViewHolder(itemView)
            }
            else -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.payment_history, parent, false)
                PaymentViewHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PaymentViewHolder -> {
                val paymentInfo = paymentInfoList[position]
                holder.borrowerName.text = paymentInfo.borrowerName
                holder.paymentAmount.text = "Payment Amount: ${paymentInfo.paymentAmount}"
                holder.paymentDate.text = "Payment Date: ${paymentInfo.date}"
                // Format the timestamp as needed and set it to timestamp TextView.

                holder.itemView.setOnClickListener {
                    onItemClick(paymentInfo.borrowerName)
                }
            }
            is EmptyViewHolder -> {
                // Handle the empty view here
                // You can set a message or hide this view as needed
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (paymentInfoList.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_PAYMENT
        }
    }

    override fun getItemCount(): Int {
        return if (paymentInfoList.isEmpty()) {
            1 // Return 1 for the empty view
        } else {
            paymentInfoList.size
        }
    }
}
