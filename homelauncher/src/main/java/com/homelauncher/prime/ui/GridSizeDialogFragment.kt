package com.homelauncher.prime.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.NumberPicker
import com.homelauncher.prime.R
import androidx.preference.PreferenceManager

class GridSizeDialogFragment : androidx.fragment.app.DialogFragment() {
    var onConfirm: ((cols: Int, rows: Int) -> Unit)? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = LayoutInflater.from(activity).inflate(R.layout.dialog_grid_size, null)
        val pickerCols = v.findViewById(R.id.gridCols) as NumberPicker
        val pickerRows = v.findViewById(R.id.gridRows) as NumberPicker
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        pickerCols.minValue = 2; pickerCols.maxValue = 8; pickerRows.minValue = 2; pickerRows.maxValue = 8
        pickerCols.value = prefs.getInt("desktop_cols", 4); pickerRows.value = prefs.getInt("desktop_rows", 5)
        return androidx.appcompat.app.AlertDialog.Builder(activity).setView(v)
            .setPositiveButton("OK") { _, _ -> onConfirm?.invoke(pickerCols.value, pickerRows.value) }
            .setNegativeButton("Cancel", null).create()
    }
}
