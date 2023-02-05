package com.example.permission_example

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.permission_example.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var listContact: ArrayList<Contact>
    lateinit var adapter: ContactAdapter

    private val permissionRequestReadContact = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ::onGotPermissionResultForReadContact
    )
    private val permissionRequestMultiple = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        ::onGotPermissionMultiple
    )

    private fun onGotPermissionResultForReadContact(granted: Boolean) {
        if (granted) {
            getContactList()
            adapter = ContactAdapter(listContact)

            binding.rv.adapter = adapter
        } else {
            if (!shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS)) {
                askUserForOpenSettings()
            } else {
                Toast.makeText(this, "Denied ;D", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onGotPermissionMultiple(grantResults: Map<String, Boolean>) {
        if (grantResults.entries.all { it.value }) {
            Toast.makeText(this, "Call phone va Send SMS granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listContact = ArrayList()

        permissionRequestReadContact.launch(android.Manifest.permission.READ_CONTACTS)

        binding.btnCall.setOnClickListener {
            permissionRequestMultiple.launch(
                arrayOf(
                    android.Manifest.permission.CALL_PHONE,
                    android.Manifest.permission.SEND_SMS
                )
            )
        }
    }


    private fun askUserForOpenSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(appSettingsIntent)
    }

    private fun getContactList() {

        val cr = contentResolver
        val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id: String = cur.getString(
                    cur.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                )
                val name: String = cur.getString(
                    cur.getColumnIndexOrThrow(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )
                if (cur.getInt(
                        cur.getColumnIndexOrThrow(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        )
                    ) > 0
                ) {
                    val pCur: Cursor? = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    var number: String? = ""
                    while (pCur?.moveToNext()!!) {
                        number = pCur.getString(
                            pCur.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )
                    }
                    pCur.close()
                    listContact.add(Contact(name, number!!))
                }
            }
        }
        cur?.close()
    }
}