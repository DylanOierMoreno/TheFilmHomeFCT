package es.barajas.thefilmhomefct.ui.inicio

import androidx.lifecycle.*

class InicioViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply { value = "This is home Fragment" }
    val text: LiveData<String> = _text
}