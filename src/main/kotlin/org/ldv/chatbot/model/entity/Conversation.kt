package org.ldv.chatbot.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Conversation(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,

    var titre: String,

    var dateCreation: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "modele_ia_id", nullable = false)
    var modeleIA: ModeleIA,

    @OneToMany(mappedBy = "conversation")
    var messages: MutableList<Message> = mutableListOf(),
    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    var utilisateur: Utilisateur
)