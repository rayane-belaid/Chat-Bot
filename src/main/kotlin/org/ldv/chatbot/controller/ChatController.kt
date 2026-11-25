package org.ldv.chatbot.controller

import org.ldv.chatbot.service.IAServiceFactory
import org.ldv.chatbot.model.dao.ConversationDAO
import org.ldv.chatbot.model.dao.MessageDAO
import org.ldv.chatbot.model.dao.ModeleIADAO
import org.ldv.chatbot.model.dao.UtilisateurDAO
import org.ldv.chatbot.model.entity.Conversation
import org.ldv.chatbot.model.entity.Message
import org.ldv.chatbot.model.entity.TypeMessage
import org.ldv.chatbot.model.entity.Utilisateur

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

import java.time.LocalDateTime
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException


@Controller
class ChatController(
    private val iaServiceFactory: IAServiceFactory,
    private val conversationDAO: ConversationDAO,
    private val messageDAO: MessageDAO,
    private val modeleIADAO: ModeleIADAO,
    private val utilisateurDAO: UtilisateurDAO
) {
    fun getUtilisateurConnecte(authentication: Authentication): Utilisateur {
        val email = authentication.name
        val utilisateur = utilisateurDAO
            .findByEmailIgnoreCase(email)
            ?: throw UsernameNotFoundException("Utilisateur introuvable")
        return utilisateur
    }
    // Page d'accueil : liste des conversations
    @GetMapping("/")
    fun accueil(model: Model,authentication: Authentication): String {
        //Recupere l'utilisateur de la session
        val utilisateur = this.getUtilisateurConnecte(authentication)
        // Ajout de l'utilisateur à la vue
        model.addAttribute("utilisateur", utilisateur)
        // Retire la conversation courante
        model.addAttribute("conversation", null)
        model.addAttribute("modeles", modeleIADAO.findAll())
        model.addAttribute("messages", emptyList<Message>())
        model.addAttribute(
            "conversations",
            conversationDAO.findAllByUtilisateurOrderByDateDerniereModificationDesc(utilisateur)
        )

        return "utilisateur/chat"
    }
    // Création d'une nouvelle conversation
    @PostMapping("/conversations")
    fun creerConversation(
        @RequestParam prompt: String,
        @RequestParam idModele: Long,
        authentication: Authentication
    ): String {

        // Récupération du modèle sélectionné
        val modele = modeleIADAO.findById(idModele)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Modèle IA introuvable"
                )
            }

        // Création de la conversation
        val conversation = Conversation(
            titre = prompt.take(50),
            dateCreation = LocalDateTime.now(),
            modeleIA = modele
        )

        val conversationEnregistree =
            conversationDAO.save(conversation)

        // Enregistrement du premier message
        val messageUtilisateur = Message(
            contenu = prompt,
            dateCreation = LocalDateTime.now(),
            typeMessage = TypeMessage.UTILISATEUR,
            conversation = conversationEnregistree
        )

        messageDAO.save(messageUtilisateur)

        try {
            // Sélection automatique du service
            val iaService = iaServiceFactory.getService(modele)

            // Appel du fournisseur correspondant
            val reponse = iaService.envoyerPrompt(prompt, modele)


            val messageAssistant = Message(
                contenu = reponse,
                dateCreation = LocalDateTime.now(),
                typeMessage = TypeMessage.ASSISTANT,
                conversation = conversationEnregistree
            )

            messageDAO.save(messageAssistant)

        } catch (e: Exception) {
            println("Erreur lors de l’appel au modèle ${modele.nom} : ${e.message}")
        }

        conversationEnregistree.dateCreation =
            LocalDateTime.now()

        conversationDAO.save(conversationEnregistree)

        return "redirect:/conversations/${conversationEnregistree.id}"
    }
    // Affichage d'une conversation
    @GetMapping("/conversations/{id}")
    fun afficherConversation(
        @PathVariable id: Long,
        model: Model,
        authentication: Authentication
    ): String {

        val conversation = conversationDAO.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

        model.addAttribute(
            "conversations",
            conversationDAO.findAllByUtilisateurOrderByDateDerniereModificationDesc(utilisateur)
        )

        model.addAttribute("conversation", conversation)
        model.addAttribute(
            "messages",
            conversation.messages.sortedBy { it.dateCreation }
        )

        return "utilisateur/chat"
    }
    // Envoi d'un nouveau message dans une conversation existante
    @PostMapping("/conversations/{id}/messages")
    fun envoyerMessage(
        @PathVariable id: Long,
        @RequestParam prompt: String,
        authentication: Authentication
    ): String {

        val conversation = conversationDAO.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

        // Enregistrement du message utilisateur
        val messageUtilisateur = Message(
            contenu = prompt,
            dateCreation = LocalDateTime.now(),
            typeMessage = TypeMessage.UTILISATEUR,
            conversation = conversation
        )

        messageDAO.save(messageUtilisateur)

        try {
            val modele = conversation.modeleIA!!
            // Sélection automatique du service
            val iaService = iaServiceFactory.getService(modele)

            // Appel du fournisseur correspondant
            val reponse = iaService.envoyerPrompt(prompt, modele)


            val messageAssistant = Message(
                contenu = reponse,
                dateCreation = LocalDateTime.now(),
                typeMessage = TypeMessage.ASSISTANT,
                conversation = conversation
            )

            messageDAO.save(messageAssistant)

        } catch (e: Exception) {
            println("Erreur lors de l'appel à ${conversation.modeleIA!!.nom} : ${e.message}")
        }

        conversation.dateCreation = LocalDateTime.now()
        conversationDAO.save(conversation)

        return "redirect:/conversations/$id"
    }
    }

