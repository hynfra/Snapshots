package com.example.snapshots

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.snapshots.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*


class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 21

    private lateinit var mBinding: ActivityMainBinding // permite acceder a los componentes externos de la activity, como los view de los fragment

    private lateinit var mActiveFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager

    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private var mFirebaseAuth: FirebaseAuth? = null
    private val authResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            Toast.makeText(this, "Bienvenido...", Toast.LENGTH_SHORT).show()
        }else{
            if(IdpResponse.fromResultIntent(it.data) == null){ // esto permite que al apretar el boton de retroceso al ingresar el usuario se salga de la app
                finish()
            }
        }
    }
    //private lateinit var  mAuth:FirebaseAuth // sirve para el ingreso anonimo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setupAuth()
        setupBottomNav()
        // ingreso anonimo, se debe habilitar tambien desde firebase
        //mAuth = FirebaseAuth.getInstance()
    }

    private fun setupAuth() { // activa los botones de acceso de google y email, ademas de proporcionar el formulario de registro
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            if(user == null){
                authResult.launch(
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false) // desactiva que pida una cuenta logeada
                        .setAvailableProviders(
                            Arrays.asList(
                                AuthUI.IdpConfig.EmailBuilder().build(),
                                AuthUI.IdpConfig.GoogleBuilder().build())
                        )
                        .build()
                )
            }
        }
    }
/*
    override fun onStart() {
        super.onStart()
      /*  // el siguiente codigo permite subir archivos a partir de una autenticacion anonima
        val user: FirebaseUser? = mAuth.getCurrentUser()
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously()
        }*/
    }*/
    /*
    private fun signInAnonymously() { // permite ingresar anonimamente para subir archivos
        mAuth.signInAnonymously().addOnSuccessListener(this, OnSuccessListener<AuthResult?> {
            // do your stuff
        })
            .addOnFailureListener{
                Snackbar.make(mBinding.root, "Error de inicio de sesion anonimo: $it",
                    Snackbar.LENGTH_SHORT).show()
            }
    }*/


    private fun setupBottomNav(){ // metodo que se encarga de configurar los fragment hacia la activity, se debe colocar en onCreate
        mFragmentManager = supportFragmentManager // variable que permite agregar los fragment
        // instancias de los 3 fragment del menu de abajo
        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()

        mActiveFragment = homeFragment

        // fragment profile
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, profileFragment, profileFragment::class.java.name)
            .hide(profileFragment).commit()
        //fragment add
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, addFragment, addFragment::class.java.name)
            .hide(addFragment).commit()
        //fragment home, no tiene hide ya que se desea que se vea siempre
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, homeFragment, homeFragment::class.java.name)
            .commit()

        // de esta forma no se pierde el progreso en cada fragment
        mBinding.bottomNav.setOnItemSelectedListener { // accion que se ejecuta al hacer click en uno de las opciones del menu de abajo
            when(it.itemId){ // recoge la id que se pincho del menu de abajo
                R.id.action_home -> { // si se apreta home ejecuta lo siguiente
                    // oculta el fragment que se esta viendo y pone el homeFragment
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment).commit()
                    mActiveFragment = homeFragment
                    true
                }
                R.id.action_add -> {
                    // oculta el fragment que se esta viendo y pone el homeFragment
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment).commit()
                    mActiveFragment = addFragment
                    true
                }
                R.id.action_profile -> {
                    // oculta el fragment que se esta viendo y pone el homeFragment
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment).commit()
                    mActiveFragment = profileFragment
                    true
                }
                else ->false
            }
        }
        mBinding.bottomNav.setOnItemReselectedListener {
            when(it.itemId){
                R.id.action_home -> (homeFragment as HomeAux).goToTop() // mueve la lista hacia arriba al pulsar el boton inicio
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener( mAuthListener)
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener(mAuthListener)
    }




}