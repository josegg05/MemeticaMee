package com.example.ragnarok.memeticamee

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.ragnarok.memeticamee.fragment.GroupFragment
import com.example.ragnarok.memeticamee.fragment.MyAccountFragment
import com.example.ragnarok.memeticamee.fragment.PeopleFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //exp1i
    private val contactsPermissionGranted
        get() = checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val REQUEST_CODE_CONTACTS_PERMISSION = 1
    }
    //exp1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //exp1
        if (!contactsPermissionGranted) {
            requestContactsPermission()
        }
        else {
            replaceFragment(PeopleFragment())

            addBtn.setOnClickListener {
                addContact()
            }
        }
        navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_people -> {
                    addBtn.show()
                    replaceFragment(PeopleFragment())
                    true
                }
                R.id.navigation_my_account -> {
                    addBtn.hide()
                    replaceFragment(MyAccountFragment())
                    true
                }
                else -> false
            }
        }


    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_layout, fragment)
                .commit()
    }


    //exp1i
    private fun requestContactsPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS)) {
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.contacts_permissions_rationale))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), REQUEST_CODE_CONTACTS_PERMISSION)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create()
                    .show();

        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), REQUEST_CODE_CONTACTS_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_CONTACTS_PERMISSION -> {
                if (grantResults[0] == 0) {
                    replaceFragment(PeopleFragment())

                    addBtn.setOnClickListener {
                        addContact()
                    }
                }
            }
            else -> replaceFragment(MyAccountFragment())
        }
    }

    //exp1f}

    //exp2i


    private fun addContact() {
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = ContactsContract.Contacts.CONTENT_TYPE
        startActivity(intent)
    }
    //exp2f
}
