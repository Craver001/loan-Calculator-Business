package dev.gameplay.loancal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.gameplay.loancal.R

class LoanAdapter(private val borrowerList: List<String>, private val onItemClick: (String) -> Unit) : RecyclerView.Adapter<LoanAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val loanNameTextView: TextView = itemView.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.borrowerlist_design, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentBorrower = borrowerList[position]
        holder.loanNameTextView.text = currentBorrower

        holder.itemView.setOnClickListener {
            onItemClick(currentBorrower)
        }
    }

    override fun getItemCount() = borrowerList.size
}
