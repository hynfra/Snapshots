package com.example.snapshots

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.snapshots.SnapshotsApplication.Companion.PATH_SNAPSHOTS
import com.example.snapshots.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class AddFragment : Fragment() {

    private lateinit var mBinding: FragmentAddBinding
    private lateinit var mStorageReference: StorageReference
    private lateinit var mDatabaseReference: DatabaseReference// no es suficiente con subir la imagen a storage, se debe extraer la url y agregarla a realtimedatabase


    private var mPhotoSelectedUrl: Uri? = null
    // reemplaza el startactivityforresult
    private val galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        // resultcode es el resultado de la consulta
        // requestCode verifica que es nuestra propia solicitud
        if(it.resultCode == Activity.RESULT_OK){ // en caso de que salga todo bien

                mPhotoSelectedUrl = it.data?.data // se coloca la data de la url
                mBinding.imgPhoto.setImageURI(mPhotoSelectedUrl)// coloca la imagen seleccionada
                mBinding.tilTitle.visibility = View.VISIBLE
                mBinding.tvMessage.text = getString(R.string.post_message_valid_title)

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {// al crearse la vista se ejecuta este metodo
        super.onViewCreated(view, savedInstanceState)
        mBinding.btnPost.setOnClickListener{ postSnapshot() }
        mBinding.btnSelect.setOnClickListener{openGallery()}
        // inicializa la storage y la database
        mStorageReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOTS) // hace referencia a la carpeta snapshots de firebase
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI) // intent que permite ingresar a las imagenes guardadas

        galleryResult.launch(intent)// inicia el intent
    }

    private fun postSnapshot() { // sube una imagen a storage
        mBinding.progressBar.visibility = View.VISIBLE // muestra el progress bar mientras carga
        val key = mDatabaseReference.push().key!! // genera el nodo y extrae la llave
        val storageReference = mStorageReference.child(PATH_SNAPSHOTS)
            // almacena los datos con el identificador como uid, que es el dato que proporciona firebase para cada usuario
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child(key)  // asigna un nombre aleatorio y unico a cada imagen

        if(mPhotoSelectedUrl != null){
            storageReference.putFile(mPhotoSelectedUrl!!) // sube el archivo que no debe ser null
                    // aqui se coloca lo que se quiere hacer mientras se sube el archivo
                .addOnProgressListener {
                    val progress = (100 * it.bytesTransferred/it.totalByteCount).toDouble()// calcula los bytes transferidos con respecto al total de bytes
                    mBinding.progressBar.progress = progress.toInt()
                    mBinding.tvMessage.text = "$progress%"

                }
                    // aca se coloca lo que se hace cuando se suba el archivo
                .addOnCompleteListener{
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                    // aca se pone lo que se hace si el archivo se subio con exito
                .addOnSuccessListener {
                    Snackbar.make(mBinding.root, "Instantanea publicada",
                        Snackbar.LENGTH_SHORT).show()
                    // aqui se extrae la URL del objeto tasksnapshot
                    it.storage.downloadUrl.addOnSuccessListener {
                        //una vez salga correcto se guarda la foto
                        saveSnapshot(key, it.toString(), mBinding.etTitle.text.toString().trim())
                        mBinding.tilTitle.visibility = View.GONE // oculta el textinputlayout
                        mBinding.tvMessage.text = getString(R.string.post_message_title) // resetea el textviewmessage
                    }
                }
                    // aca se pone lo que se hace si llega a fallar al subir el archivo
                .addOnFailureListener{
                    Snackbar.make(mBinding.root, "No se pudo subir, intente mas tarde",
                        Snackbar.LENGTH_SHORT).show()
                }
        }


    }

    private fun saveSnapshot(key: String, url: String, title: String){ // guarda la url dentro de realdatabase

        val snapshot = Snapshot(title = title, photoUrl = url)

        mDatabaseReference.child(key).setValue(snapshot) // pasa el objeto con la key

    }




}