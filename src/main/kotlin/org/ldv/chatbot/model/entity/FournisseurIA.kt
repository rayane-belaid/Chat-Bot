package org.ldv.chatbot.model.entity

import jakarta.persistence.*

@Entity
class FourniseurIA(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    var nom: String,

    var cheminLogo: String,

    var description: String,

    @OneToMany(mappedBy = "fourniseurIA")
    var modeles: MutableList<ModeleIA> = mutableListOf()

)