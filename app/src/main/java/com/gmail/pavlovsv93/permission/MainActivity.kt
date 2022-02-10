package com.gmail.pavlovsv93.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.gmail.pavlovsv93.permission.databinding.ActivityMainBinding

const val REQUEST_CODE =
    42 // Код результата ответа, ожидаемый при получении подтверждения (задается разработчиком)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = this

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
        context.let {
            when {
                // Разрешение дано
                ContextCompat.checkSelfPermission(it, android.Manifest.permission.READ_CONTACTS) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    //Выполняется если разрешение на допуск ЕСТЬ
                    getContacts()
                }
                // указать поеснение перед подтверждением
                shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> { // Вызов пояснения, для чего нужен доступ
                    AlertDialog.Builder(it)
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
                    context?.let {
                        // Вслучае вызова AlertDialog с activity необходимо передать контекст активити,
                        // в ином случае it (контекст, от которого запущен AleartDialog)
                        AlertDialog.Builder(it)
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

        context.let {
            // Получаем ContactResolver у контакта
            val contactResolver: ContentResolver = it.contentResolver

            //Отправляем запрос на получение контактов и получаем ответ  в виде Cursor
            val cursorWithContacts: Cursor? = contactResolver
                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)

            cursorWithContacts?.let { cursor ->
                for (i in 0..cursor.count) {
                    if (cursor.moveToPosition(i)) {
                        val uid = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
                        val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        val photo = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                        val number = cursor?.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        addView(it, uid, name, number, photo)
                    }
                }
            }
            // Закрыть Cursor
            cursorWithContacts?.close()
        }

    }

    private fun addView(context: Context, uid: Long, name: String?, phone: String?, photo: String?) {
        binding.containerContacts.addView(Button(context).apply {
            text = (uid.toString() + " " + name + "\n" + phone + "\n" + photo)
            textSize = resources.getDimension(R.dimen.text_size)
            setOnClickListener {
                val intentR = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intentR);
                }
            }
        })
    }
}