package com.example.electronic_journal.fragment.administrator

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.electronic_journal.databinding.FragmentAdministratorSearchBinding

class AdministratorSearchFragment : Fragment() {

    private lateinit var binding: FragmentAdministratorSearchBinding
    private val viewModel: AdministratorSearchViewModel by viewModels()

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

        // Подписываемся на LiveData для обновления ответа
        viewModel.answerText.observe(viewLifecycleOwner) { answer ->
            binding.tvInternetInfo.text = answer
        }

        // Управление видимостью кнопки "Обновить":
        // Кнопка показывается только если загрузка завершена и произошла ошибка.
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.btnReload.visibility = View.GONE
            } else {
                // Если не загрузка, проверяем флаг ошибки
                binding.btnReload.visibility =
                    if (viewModel.errorOccurred.value == true) View.VISIBLE else View.GONE
            }
        }
        // Дополнительно можно подписаться на изменения флага ошибки
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

        // Получаем EditText из TextInputLayout
        val editText = binding.searchInputLayout.editText

        // Обработка нажатия на иконку очистки (крестик)
        binding.searchInputLayout.setEndIconOnClickListener {
            binding.searchInputLayout.editText?.text?.clear()
            hideKeyboard()
        }

        // Кнопка "Поиск": если поле пустое, показываем Toast, иначе отправляем запрос
        binding.searchInputLayout.setStartIconOnClickListener {
            val query = editText?.text?.toString()?.trim().orEmpty()
            if (query.isEmpty()) {
                Toast.makeText(context, "Введите запрос", Toast.LENGTH_SHORT).show()
            } else {
                hideKeyboard()
                viewModel.sendQuery(query)
            }
        }

        // Кнопка "Обновить" — повторяем последний запрос
        binding.btnReload.setOnClickListener {
            hideKeyboard()
            viewModel.lastQuery.value?.let { lastQuery ->
                viewModel.sendQuery(lastQuery)
            }
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
}
