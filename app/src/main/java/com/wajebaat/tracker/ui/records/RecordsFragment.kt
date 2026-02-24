package com.wajebaat.tracker.ui.records

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.wajebaat.tracker.R
import com.wajebaat.tracker.data.local.Entry
import com.wajebaat.tracker.databinding.FragmentRecordsBinding
import com.wajebaat.tracker.ui.EntryViewModel

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels()
    private lateinit var adapter: EntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EntryAdapter(
            onEditClick = { entry -> showEditDialog(entry) },
            onDeleteClick = { entry -> showDeleteConfirmation(entry) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RecordsFragment.adapter
        }

        viewModel.allEntries.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
            if (entries.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun showEditDialog(entry: Entry) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_entry, null)
        dialog.setContentView(dialogView)

        val tilAmount = dialogView.findViewById<TextInputLayout>(R.id.tilEditAmount)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etEditAmount)
        val btnUpdate = dialogView.findViewById<android.widget.Button>(R.id.btnUpdate)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancelEdit)

        etAmount.setText(entry.amount.toString())

        // Show preview
        val tvPreviewWajebaat = dialogView.findViewById<android.widget.TextView>(R.id.tvEditPreviewWajebaat)
        val tvPreviewMoneyLeft = dialogView.findViewById<android.widget.TextView>(R.id.tvEditPreviewMoneyLeft)
        tvPreviewWajebaat.text = "Wajebaat: ₹%.2f".format(entry.wajebaat)
        tvPreviewMoneyLeft.text = "Money Left: ₹%.2f".format(entry.moneyLeft)

        etAmount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val amt = s.toString().toDoubleOrNull() ?: return
                val w = amt / 5.0
                val ml = amt - w
                tvPreviewWajebaat.text = "Wajebaat: ₹%.2f".format(w)
                tvPreviewMoneyLeft.text = "Money Left: ₹%.2f".format(ml)
            }
        })

        btnUpdate.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()
            if (amountStr.isEmpty()) {
                tilAmount.error = "Please enter an amount"
                return@setOnClickListener
            }
            val newAmount = amountStr.toDoubleOrNull()
            if (newAmount == null || newAmount <= 0) {
                tilAmount.error = "Please enter a valid positive amount"
                return@setOnClickListener
            }
            viewModel.updateEntry(entry, newAmount)
            Toast.makeText(requireContext(), "Entry updated!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showDeleteConfirmation(entry: Entry) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this entry?\n\nAmount: ₹%.2f\nDate: %s".format(entry.amount, entry.date))
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteEntry(entry)
                Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
