package com.gmail.pavlovsv93.permission.content.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.gmail.pavlovsv93.permission.R
import com.gmail.pavlovsv93.permission.content.provider.db.*

private const val URI_ALL = 1   // URI для всех записей
private const val URI_ID = 2    // URI для одной записи
private const val ENTITY_PATH = "Entity"

class MyContentProvider : ContentProvider() {

    private var authorities: String? = null             // Адрес URI
    private lateinit var contentUri: Uri                // URI адрес Content Provider
    private lateinit var uriMatcher: UriMatcher         // Определитель типа адреса URI

    private var entityContentType: String? = null       //Тип данных из набора строк
    private var entityContentItemType: String? = null   //Тип данных однострочный


    //Вызывается для инициализации Content Provider
    override fun onCreate(): Boolean {
        // Получаем часть URI из ресурсов
        authorities = context?.resources?.getString(R.string.authorities)

        uriMatcher =
            UriMatcher(UriMatcher.NO_MATCH)                        //Инициализируем вспомогательный класс
        uriMatcher.addURI(authorities, ENTITY_PATH, URI_ALL)                //Добавляем все объекты
        uriMatcher.addURI(authorities, "$ENTITY_PATH/#", URI_ID)       //Добавляем объект по ID

        entityContentType = "vnd.android.cursor.dir/vnd.$authorities.$ENTITY_PATH"
        entityContentItemType = "vnd.android.cursor.item/vnd.$authorities.$ENTITY_PATH"

        contentUri =
            Uri.parse("content://$authorities/$ENTITY_PATH")   // Строка допуска к Content Provider
        return true
    }

    // Возвращает данные типа MIME в Content Provider
    override fun getType(uri: Uri): String? {
        when (uriMatcher.match(uri)) {
            URI_ALL -> return entityContentType     // Вернет массив объектов
            URI_ID -> return entityContentItemType  // Вернет один объект
        }
        return null                                 // Иначе вернет ничего (null)
    }

    //Возвращает данные вызываемому абаненту
    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val dao: DAO = App.getDao()
        val cursor = when (uriMatcher.match(uri)) {
            URI_ALL -> {
                dao.getEntityCursor()                   // Запрос к БД для всех элементов
            }
            URI_ID -> {
                val id = ContentUris.parseId(uri)       //Определяем ID из URI
                dao.getEntityCursorId(id = id)          //Запрос к БД для обного элемента
            }
            else -> throw IllegalArgumentException("Wrong: $uri")
        }

        cursor.setNotificationUri(
            context!!.contentResolver,
            contentUri
        )    //Устанавливаем нотификацию для изменения данных
        return cursor
    }

    // Удаляет существующие данные Content Provider
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        require(uriMatcher.match(uri) == URI_ID) {                  // Проверка соответствия URI
            "Wrong: $uri"
        }
        val dao: DAO = App.getDao()
        val id = ContentUris.parseId(uri)                               // Получение ID из URI
        dao.deleteById(id = id)                                         // Удаление записи по ID
        context?.contentResolver?.notifyChange(uri, null)       //Нотификация на изменение Cursor

        return 1
    }

    //Добавляет новые данные в Content Provider
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        require(uriMatcher.match(uri) == URI_ID) {                  // Проверка соответствия URI
            "Wrong: $uri"
        }
        val dao: DAO = App.getDao()
        val entity: Entity = retype(values)// Добавляем записть
        val id: Long = entity.uid
        dao.insert(entity)                                              // Добавить запись
        val resultUri = ContentUris.withAppendedId(contentUri, id)
        context?.contentResolver?.notifyChange(
            resultUri,
            null
        ) // Нотификация на изменение Cursor по адресу resultUri
        return resultUri
    }

    private fun retype(values: ContentValues?): Entity {
        return if (values == null) {
            Entity()
        } else {
            val uid = if (values.containsKey(UID)) values[UID] as Long else 0
            val title = values[TITLE] as String
            val mass = values[MASS] as String
            Entity(uid, title, mass)
        }
    }


    // Обновляет существующие данные в Content Provider
    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        require(uriMatcher.match(uri) == URI_ID) {                 // Проверка соответствия URI
            "Wrong: $uri"
        }
        val dao: DAO = App.getDao()
        dao.update(retype(values))                                      // Обновление записи
        context!!.contentResolver?.notifyChange(uri, null)      // Нотификация на изменение Cursor
        return 1
    }
}