package com.example.electronic_journal.fragment.administrator

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.electronic_journal.databinding.FragmentAdministratorSearchBinding
import com.google.android.material.textfield.TextInputEditText

class AdministratorSearchFragment : Fragment() {

    private lateinit var binding: FragmentAdministratorSearchBinding
    private val viewModel: AdministratorSearchViewModel by viewModels()
    private val prefs by lazy {
        requireContext().getSharedPreferences("search_history", Context.MODE_PRIVATE)
    }
    // Для автоотправки запроса по таймеру 2 секунды
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    // ListPopupWindow для отображения истории поиска
    private lateinit var listPopupWindow: ListPopupWindow

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdministratorSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Восстанавливаем текст запроса, если он есть
        savedInstanceState?.getString("search_query")?.let { savedText ->
            binding.searchInputLayout.editText?.setText(savedText)
        }

        // Инициализируем ListPopupWindow для истории поиска
        listPopupWindow = ListPopupWindow(requireContext()).apply {
            // Крепим к TextInputLayout (можно использовать и само поле ввода)
            anchorView = binding.searchInputLayout
            // Задаём ширину, например, равную ширине поля ввода
            width = ListPopupWindow.MATCH_PARENT
            isModal = true
        }

        // Подписываемся на LiveData для обновления ответа
        viewModel.answerText.observe(viewLifecycleOwner) { answer ->
            binding.tvInternetInfo.text = answer
        }

        // Управление видимостью кнопки "Обновить" и ProgressBar
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.btnReload.visibility = View.GONE
                binding.progressBar?.visibility = View.VISIBLE
            } else {
                binding.progressBar?.visibility = View.GONE
                binding.btnReload.visibility =
                    if (viewModel.errorOccurred.value == true) View.VISIBLE else View.GONE
            }
        }

        viewModel.errorOccurred.observe(viewLifecycleOwner) { errorOccurred ->
            if (!viewModel.isLoading.value!! && errorOccurred) {
                binding.btnReload.visibility = View.VISIBLE
            } else {
                binding.btnReload.visibility = View.GONE
            }
        }

        // Кнопка "Назад"
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Получаем TextInputEditText из TextInputLayout
        val editText = binding.searchInputLayout.editText as TextInputEditText

        // Показываем историю поиска при получении фокуса
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchHistory(editText)
            } else {
                listPopupWindow.dismiss()
            }
        }

        // Обработка нажатия элемента из ListPopupWindow
        listPopupWindow.setOnItemClickListener { parent, _, position, _ ->
            val selectedQuery = parent.getItemAtPosition(position) as String
            editText.setText(selectedQuery)
            addQueryToHistory(selectedQuery)
            listPopupWindow.dismiss()
            hideKeyboard()
            viewModel.sendQuery(selectedQuery)
        }

        // Обработка нажатия на иконку очистки (крестик)
        binding.searchInputLayout.setEndIconOnClickListener {
            editText.text?.clear()
            hideKeyboard()
        }

        // Текстовый слушатель для автоотправки запроса через 2 секунды бездействия
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Сбрасываем предыдущий таймер
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                // Если текст не пустой, ставим новую задержку
                val query = s?.toString()?.trim().orEmpty()
                if (query.isNotEmpty()) {
                    searchRunnable = Runnable {
                        addQueryToHistory(query)
                        viewModel.sendQuery(query)
                    }
                    searchHandler.postDelayed(searchRunnable!!, 2000)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Кнопка "Поиск": если поле пустое, показываем Toast, иначе отправляем запрос
        binding.searchInputLayout.setStartIconOnClickListener {
            searchRunnable?.let { searchHandler.removeCallbacks(it) }
            val query = editText.text?.toString()?.trim().orEmpty()
            if (query.isEmpty()) {
                Toast.makeText(context, "Введите запрос", Toast.LENGTH_SHORT).show()
            } else {
                hideKeyboard()
                addQueryToHistory(query)
                viewModel.sendQuery(query)
            }
        }

        // Кнопка "Обновить" — повторяем последний запрос
        binding.btnReload.setOnClickListener {
            hideKeyboard()
            viewModel.lastQuery.value?.let { lastQuery ->
                addQueryToHistory(lastQuery)
                viewModel.sendQuery(lastQuery)
            }
        }

        // Кнопка "Очистить историю" — очищаем SharedPreferences и закрываем ListPopupWindow
        binding.btnClearHistory?.setOnClickListener {
            clearSearchHistory()
            listPopupWindow.dismiss()
            Toast.makeText(context, "История поиска очищена", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Сохраняем текст поискового запроса при повороте экрана.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val query = binding.searchInputLayout.editText?.text?.toString() ?: ""
        outState.putString("search_query", query)
    }

    /**
     * Скрыть клавиатуру принудительно.
     */
    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = activity?.currentFocus
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // region Методы работы с историей поиска

    private fun showSearchHistory(editText: TextInputEditText) {
        val history = loadSearchHistory()
        if (history.isNotEmpty()) {
            // Создаем адаптер для ListPopupWindow
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, history)
            listPopupWindow.setAdapter(adapter)
            listPopupWindow.show()
        }
    }

    private fun loadSearchHistory(): MutableList<String> {
        val historyString = prefs.getString("history", "") ?: ""
        return if (historyString.isNotEmpty()) {
            historyString.split("||").toMutableList()
        } else {
            mutableListOf()
        }
    }

    private fun saveSearchHistory(history: List<String>) {
        val historyString = history.joinToString("||")
        prefs.edit().putString("history", historyString).apply()
    }

    private fun addQueryToHistory(query: String) {
        if (query.isBlank()) return
        val history = loadSearchHistory()
        history.remove(query)
        history.add(0, query)
        if (history.size > 10) {
            history.subList(10, history.size).clear()
        }
        saveSearchHistory(history)
    }

    private fun clearSearchHistory() {
        prefs.edit().remove("history").apply()
    }
    // endregion
}
