package com.wajebaat.tracker.ui.summary

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.wajebaat.tracker.databinding.FragmentSummaryBinding
import com.wajebaat.tracker.ui.EntryViewModel

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            binding.tvTotalAmount.text = "₹%.2f".format(total ?: 0.0)
        }

        viewModel.totalWajebaat.observe(viewLifecycleOwner) { total ->
            binding.tvTotalWajebaat.text = "₹%.2f".format(total ?: 0.0)
        }

        viewModel.totalMoneyLeft.observe(viewLifecycleOwner) { total ->
            binding.tvTotalMoneyLeft.text = "₹%.2f".format(total ?: 0.0)
        }

        viewModel.entryCount.observe(viewLifecycleOwner) { count ->
            binding.tvEntryCount.text = count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
