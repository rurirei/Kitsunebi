package `fun`.kitsunebi.kitsunebi4android.ui.perapp

import `fun`.kitsunebi.kitsunebi4android.R
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.github.rurirei.kitsunebi.ConstantUtil
import io.github.rurirei.kitsunebi.PreferencesUtil.get
import io.github.rurirei.kitsunebi.PreferencesUtil.put
import io.github.rurirei.kitsunebi.PreferencesUtil.sharedPreferences
import java.io.IOException


class PerAppFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {

    private val export by lazy { findPreference<Preference>(getString(R.string.export_list))!! }
    private val import by lazy { findPreference<Preference>(getString(R.string.import_list))!! }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.per_app, rootKey)
        export.onPreferenceClickListener = this
        import.onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        return when (preference) {
            export -> { export(); true }
            import -> { import(); true }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ConstantUtil.INTENT.REQUEST_FILE_EXPORT -> {
                    (data?.data ?: return).let { uri ->
                        requireActivity().contentResolver.openOutputStream(newFile(requireActivity().contentResolver, uri, "rule.json", "application/json"))?.use { outputStream ->
                            try {
                                AppList(allowedList = allowedList.split(",").filter { it.isNotEmpty() }.asSequence().sorted().toList(), disallowedList = disallowedList.split(",").filter { it.isNotEmpty() }.asSequence().sorted().toList()).let {
                                    Gson().toJson(it, AppList::class.java).format().byteInputStream(Charsets.UTF_8).copyTo(outputStream)
                                }
                            } catch (ignored: NullPointerException) {
                            } catch (ignored: com.google.gson.JsonSyntaxException) {
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            outputStream.close()
                        }
                    }
                }
                ConstantUtil.INTENT.REQUEST_FILE_IMPORT-> {
                    (data?.data ?: return).let { uri ->
                        requireActivity().contentResolver.openInputStream(uri)?.buffered()?.use { inputStream ->
                            try {
                                Gson().fromJson(inputStream.bufferedReader().readText().format(), AppList::class.java).let {
                                    requireActivity().sharedPreferences.put(getString(R.string.per_app_allowed_app_list), it.allowedList.filter { l -> l.isNotEmpty() }.joinToString(separator = ","))
                                    requireActivity().sharedPreferences.put(getString(R.string.per_app_disallowed_app_list), it.disallowedList.filter { l -> l.isNotEmpty() }.joinToString(separator = ","))
                                }
                            } catch (ignored: NullPointerException) {
                            } catch (ignored: com.google.gson.JsonSyntaxException) {
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            inputStream.close()
                        }
                    }
                }
                else -> {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    @Throws(com.google.gson.JsonSyntaxException::class, NullPointerException::class)
    private fun String.format(): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(this))
    }

    private fun newFile(contentResolver: ContentResolver, uri: Uri, displayName: String, mimeType: String): Uri {
        return DocumentsContract.createDocument(contentResolver, uri, mimeType, displayName) ?: throw IOException()
    }

    private fun export() {
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.DIRECTORY_DOWNLOADS)
        }.let {
            startActivityForResult(it, ConstantUtil.INTENT.REQUEST_FILE_EXPORT)
        }
    }

    private fun import() {
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }.let {
            startActivityForResult(it, ConstantUtil.INTENT.REQUEST_FILE_IMPORT)
        }
    }

    private val allowedList: String get() =
            requireActivity().sharedPreferences.get(getString(R.string.per_app_allowed_app_list), "")

    private val disallowedList: String get() =
            requireActivity().sharedPreferences.get(getString(R.string.per_app_disallowed_app_list), "")

    private data class AppList(val allowedList: List<String>,
                               val disallowedList: List<String>)

}
