package com.example.snapshots

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
//siempre se crea una data class para cada objeto
//IgnoreExtraProperties es para firebase
@IgnoreExtraProperties
data class Snapshot(@get:Exclude var id: String = "",// @get:Exclude excluye el campo al usar el objeto
                    var title:String = "",
                    var photoUrl: String = "",
                    var likelist: Map <String, Boolean> = mutableMapOf())
