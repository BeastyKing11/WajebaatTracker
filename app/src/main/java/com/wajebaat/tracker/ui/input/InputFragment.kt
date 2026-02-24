package com.wajebaat.tracker.ui.input

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.wajebaat.tracker.databinding.FragmentInputBinding
import com.wajebaat.tracker.ui.EntryViewModel

class InputFragment : Fragment() {

    private var _binding: FragmentInputBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.setOnClickListener {
            val amountStr = binding.etAmount.text.toString().trim()
            if (amountStr.isEmpty()) {
                binding.tilAmount.error = "Please enter an amount"
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.tilAmount.error = "Please enter a valid positive amount"
                return@setOnClickListener
            }
            binding.tilAmount.error = null

            // Show preview
            val wajebaat = amount / 5.0
            val moneyLeft = amount - wajebaat
            binding.tvPreviewWajebaat.text = "Wajebaat: ₹%.2f".format(wajebaat)
            binding.tvPreviewMoneyLeft.text = "Money Left: ₹%.2f".format(moneyLeft)
            binding.cardPreview.visibility = View.VISIBLE

            viewModel.addEntry(amount)
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Entry saved successfully!", Toast.LENGTH_SHORT).show()
                binding.etAmount.text?.clear()
                binding.cardPreview.visibility = View.GONE
                viewModel.resetSaveSuccess()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
