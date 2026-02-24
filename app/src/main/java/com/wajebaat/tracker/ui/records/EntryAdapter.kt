package com.wajebaat.tracker.ui.records

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wajebaat.tracker.data.local.Entry
import com.wajebaat.tracker.databinding.ItemEntryBinding

class EntryAdapter(
    private val onEditClick: (Entry) -> Unit,
    private val onDeleteClick: (Entry) -> Unit
) : ListAdapter<Entry, EntryAdapter.EntryViewHolder>(EntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EntryViewHolder(private val binding: ItemEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: Entry) {
            binding.tvDate.text = entry.date
            binding.tvAmount.text = "₹%.2f".format(entry.amount)
            binding.tvWajebaat.text = "₹%.2f".format(entry.wajebaat)
            binding.tvMoneyLeft.text = "₹%.2f".format(entry.moneyLeft)

            binding.btnEdit.setOnClickListener { onEditClick(entry) }
            binding.btnDelete.setOnClickListener { onDeleteClick(entry) }
        }
    }

    class EntryDiffCallback : DiffUtil.ItemCallback<Entry>() {
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem
    }
}
