package com.wajebaat.tracker.ui.settings

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.wajebaat.tracker.databinding.FragmentSettingsBinding
import com.wajebaat.tracker.ui.EntryViewModel
import com.wajebaat.tracker.utils.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels()
    private lateinit var themePreferences: ThemePreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        themePreferences = ThemePreferences(requireContext())

        lifecycleScope.launch {
            val isDark = themePreferences.isDarkMode.first()
            binding.switchDarkMode.isChecked = isDark
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                themePreferences.setDarkMode(isChecked)
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        binding.btnDeleteAll.setOnClickListener { showDeleteAllConfirmation() }
        binding.btnExportCsv.setOnClickListener { exportCsv() }
        binding.btnExportExcel.setOnClickListener { exportExcel() }
    }

    private fun showDeleteAllConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete All Data")
            .setMessage("Are you sure you want to delete ALL entries? This action cannot be undone.")
            .setPositiveButton("Delete All") { _, _ ->
                viewModel.deleteAllEntries()
                toast("All data deleted")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportCsv() {
        lifecycleScope.launch {
            // Fetch entries on IO thread
            val entries = withContext(Dispatchers.IO) { viewModel.getAllEntriesSync() }

            if (entries.isEmpty()) {
                toast("No data to export")
                return@launch
            }

            toast("Exporting CSV…")

            // exportToCsv already switches to Dispatchers.IO internally
            val result = ExportUtils.exportToCsv(requireContext(), entries)

            result
                .onSuccess { file ->
                    toast("CSV saved: ${file.name}")
                    shareFile(file)
                }
                .onFailure { e ->
                    toast("CSV export failed: ${e.message ?: "Unknown error"}")
                    e.printStackTrace()
                }
        }
    }

    private fun exportExcel() {
        lifecycleScope.launch {
            setExcelButtonState(enabled = false, label = "Exporting…")

            // Fetch entries on IO thread
            val entries = withContext(Dispatchers.IO) { viewModel.getAllEntriesSync() }

            if (entries.isEmpty()) {
                toast("No data to export")
                setExcelButtonState(enabled = true, label = "Export to Excel (.xlsx)")
                return@launch
            }

            // exportToExcel already switches to Dispatchers.IO internally
            val result = ExportUtils.exportToExcel(requireContext(), entries)

            setExcelButtonState(enabled = true, label = "Export to Excel (.xlsx)")

            result
                .onSuccess { file ->
                    toast("Excel saved: ${file.name}")
                    shareFile(file)
                }
                .onFailure { e ->
                    toast("Excel export failed: ${e.message ?: "Unknown error"}")
                    e.printStackTrace()
                }
        }
    }

    private fun shareFile(file: java.io.File) {
        try {
            val intent = ExportUtils.getShareIntent(requireContext(), file)
            startActivity(android.content.Intent.createChooser(intent, "Share export via…"))
        } catch (e: Exception) {
            // Share sheet unavailable (e.g. emulator) — file is already saved, that's fine
            toast("File saved to: ${file.absolutePath}")
        }
    }

    private fun setExcelButtonState(enabled: Boolean, label: String) {
        binding.btnExportExcel.isEnabled = enabled
        binding.btnExportExcel.text = label
    }

    private fun toast(msg: String) {
        if (isAdded) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
