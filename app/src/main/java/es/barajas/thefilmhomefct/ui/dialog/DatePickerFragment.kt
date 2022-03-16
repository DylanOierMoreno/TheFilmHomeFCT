package es.barajas.thefilmhomefct.ui.dialog

import android.app.*
import android.os.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import es.barajas.thefilmhomefct.R
import java.util.*

/*Fragment según el cual se construye el DateTimePicker que utilizamos en Registro.kt para
  que los usuarios puedan escoger su fecha de nacimiento en un formato Spinner */
class DatePickerFragment : DialogFragment() {
    private var listener: DatePickerDialog.OnDateSetListener? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(requireContext(), R.style.dlg_datePicker, listener,
            year, month, day)
        return datePickerDialog
    }

    companion object {
        fun newInstance(listener: DatePickerDialog.OnDateSetListener): DatePickerFragment {
            val fragment = DatePickerFragment()
            fragment.listener = listener
            return fragment
        }
    }
}