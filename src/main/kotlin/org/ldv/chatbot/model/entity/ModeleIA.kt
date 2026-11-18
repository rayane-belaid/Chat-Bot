package org.ldv.chatbot.model.entity

import jakarta.persistence.*

@Entity
class ModeleIA(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    var nom: String,

    var urlAPI: String,

    var description: String,

    var cheminLogo: String,

    @ManyToOne
    @JoinColumn(name = "fourniseur_ia_id", nullable = false)
    var fourniseurIA: FourniseurIA

)