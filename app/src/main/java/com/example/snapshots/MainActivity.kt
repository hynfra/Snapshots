package com.example.snapshots

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.snapshots.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding // permite acceder a los componentes externos de la activity, como los view de los fragment

    private lateinit var mActiveFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setupBottomNav()
    }

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
    }
}