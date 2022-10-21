package com.example.snapshots

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.snapshots.databinding.FragmentHomeBinding
import com.example.snapshots.databinding.ItemSnapshotBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase


class HomeFragment : Fragment() {
    private lateinit var mBinding: FragmentHomeBinding
    // se declarara un adaptador con firebaseUI
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot,SnapshotHolder> // el adaptador recibe el tipo de dato y el recyclerview

    private lateinit var mLayoutManager: RecyclerView.LayoutManager // crea instancia del recyclerview

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // se aplica inflate con el mbinding
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    // se debe crear el adaptador en el onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // se deben crear 2 constantes para el adaptador
        // ruta donde se llamara la informacion, se llaman nodos, se le debe poner un nombre "snapshots"
        val query = FirebaseDatabase.getInstance().reference.child("snapshots")
        val options = FirebaseRecyclerOptions.Builder<Snapshot>() //construye las opciones del recycler en base a la entidad
            .setQuery(query, Snapshot::class.java).build()// pone donde se llamara la informacion, poniendo su clase

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot,SnapshotHolder>(options){
            //metodos requeridos para el adaptador
            private lateinit var mContext : Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {// aca se pone la vista donde se pondra el recycler
                //en tod o adaptador se debe poner el contexto
                mContext = parent.context // pone en el viewGroup el contexto

                val view = LayoutInflater.from(mContext).inflate(R.layout.item_snapshot, parent, false) // pone el fragment item_snapshot
                return SnapshotHolder(view)
            }

            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {
                val snapshot = getItem(position)
                with(holder){
                    setListener(snapshot)

                    binding.tvTitle.text = snapshot.title // pasa titulo
                    Glide.with(mContext) // carga imagen
                        .load(snapshot.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgPhoto)
                }
            }

            override fun onDataChanged() { // detiene el progress bar
                super.onDataChanged()
                mBinding.progressBar.visibility = View.GONE
            }

            override fun onError(error: DatabaseError) { // error de database
                super.onError(error)
                Toast.makeText(mContext, error.message, Toast.LENGTH_SHORT).show()
            }

        }
        mLayoutManager = LinearLayoutManager(context) // pone el recyclerview en el linearlayout con este contexto

        mBinding.recyclerView.apply { // pone dentro del recyclerview cada parametro cread
            setHasFixedSize(true)
            layoutManager = mLayoutManager // pone el layout
            adapter = mFirebaseAdapter// pone el adaptador
        }
    }

    override fun onStart() { // metodo sobreescrito que permitira definir cuando se obtendran los datos para el recycler
        super.onStart()
        mFirebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }
// recyclerview permite mostrar los datos
    // se debe usar ViewHolder como contenedor de las vistas
    //el adaptador recibe los datos y los almacena en el contenedor
    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view){ // clase del recycler view de snapshot
        val binding = ItemSnapshotBinding.bind(view) // hace referencia la layout de snapshot

        fun setListener(snapshot: Snapshot){

        }

    }
}