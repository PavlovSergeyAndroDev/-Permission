package com.gmail.pavlovsv93.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.gmail.pavlovsv93.permission.databinding.ActivityMainBinding

const val REQUEST_CODE =
    42 // Код результата ответа, ожидаемый при получении подтверждения (задается разработчиком)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //подключение viewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnContacts.setOnClickListener {
            checkPermission()
        }
    }

    private fun checkPermission() {
        // работает от контекста
        applicationContext?.let {
            when {
                // Разрешение дано
                ContextCompat.checkSelfPermission(it, android.Manifest.permission.READ_CONTACTS) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    //Выполняется если разрешение на допуск ЕСТЬ
                    getContacts()
                }
                // указать поеснение перед подтверждением
                shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> { // Вызов пояснения, для чего нужен доступ
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Доступ к контактам")                  // Задать заголовок окна пояснения
                        .setMessage("Объяснение необходимости доступа")                     // Задать сообщение окна пояснения
                        .setPositiveButton("Предоставить доступ") { _, _ ->              // Задать кнопку и текст кнопки подтверждения
                            request()                                                // перезапросить на подтверждение доступа
                        }
                        .setNegativeButton("НЕТ") { dialog, _ ->                         // Задать кнопку и тект кнопки отказа
                            dialog.dismiss()                                                    // Скрыть окно запроса разрешения по нажатию кнопки
                        }
                        .create()                                                           //
                        .show()                                                             // Показать диалоговое окно
                }
                else -> request()        // Запросить разрешение
            }
        }
    }

    private fun request() {
        //arrayOf помещаются все запросы на допуск необходимые для работы приложения, в данной ситуации 1 из 1
        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE)

        //как пример на два запроса
        // requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS,  Manifest.permission.CAMERA), REQUEST_CODE)
    }

    //Переопределить метод onRequestPermissionsResult
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE -> {
                //Проверка полученого разрешения
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContacts()
                } else {
                    // Пояснение пользователю, почему экран остается пустым или дальнейшее действие не доступно
                    applicationContext?.let {
                        // Вслучае вызова AlertDialog с activity необходимо передать контекст активити,
                        // в ином случае it (контекст, от которого запущен AleartDialog)
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Заголовок")
                            .setMessage("Сообщение")
                            .setNegativeButton("Закрыть") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                }
            }
        }
    }

    // обработка при достижении согласия
    @SuppressLint("Range")
    private fun getContacts() {

        applicationContext?.let {
            // Получаем ContactResolver у контакта
            val contactResolver: ContentResolver = it.contentResolver

            //Отправляем запрос на получение контактов и получаем ответ  в виде Cursor
            val cursorWithContacts: Cursor? = contactResolver
                .query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC"
                )

            cursorWithContacts?.let { cursor ->
                for (i in 0..cursor.count) {
                    if (cursor.moveToPosition(i)) {
                        val name = cursor.getString(
                            cursor.getColumnIndex(
                                ContactsContract.Contacts.DISPLAY_NAME
                            )
                        )
                        val phone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                        addView(it, name, phone)
                    }
                }
            }
            // Закрыть Cursor
            cursorWithContacts?.close()
        }

    }

    private fun addView(context: Context, name: String?, phone: String?) {
        binding.containerContacts.addView(AppCompatTextView(context).apply {
            text = name + "\n" + phone
            textSize = resources.getDimension(R.dimen.text_size)
        })
    }
}