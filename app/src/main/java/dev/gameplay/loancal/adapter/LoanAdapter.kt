package dev.gameplay.loancal.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.gameplay.loancal.R

class LoanAdapter(
    private val borrowerList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_EMPTY = 0
    private val VIEW_TYPE_BORROWER = 1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val loanNameTextView: TextView = itemView.findViewById(R.id.name)
    }

    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.empty_borrower_layout, parent, false)
                EmptyViewHolder(itemView)
            }
            else -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.borrowerlist_design, parent, false)
                ViewHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val currentBorrower = borrowerList[position]
                holder.loanNameTextView.text = currentBorrower

                holder.itemView.setOnClickListener {
                    onItemClick(currentBorrower)
                }
            }
            is EmptyViewHolder -> {
                // Handle the empty view here
                // You can set a message or hide this view as needed
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (borrowerList.isEmpty()) {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_BORROWER
        }
    }

    override fun getItemCount(): Int {
        return if (borrowerList.isEmpty()) {
            1 // Return 1 for the empty view
        } else {
            borrowerList.size
        }
    }
}

