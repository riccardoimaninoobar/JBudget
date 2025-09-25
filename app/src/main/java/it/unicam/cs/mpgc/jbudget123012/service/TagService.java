package it.unicam.cs.mpgc.jbudget123012.service;

import it.unicam.cs.mpgc.jbudget123012.model.Tag;
import it.unicam.cs.mpgc.jbudget123012.repository.TagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javafx.scene.control.TreeItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servizio per la logica di business dei tag.
 * Gestisce le operazioni complesse e le transazioni.
 */
public class TagService {

    private final TagRepository tagRepository;
    private final EntityManagerFactory emf;

    /**
     * Costruttore per l'iniezione delle dipendenze necessarie.
     */
    public TagService(TagRepository tagRepository, EntityManagerFactory emf) {
        this.tagRepository = tagRepository;
        this.emf = emf;
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    /**
     * Crea un nuovo tag gestendo la transazione manualmente.
     *
     * @param name nome del nuovo tag
     * @param parent tag genitore (può essere null)
     * @return il tag appena creato
     */
    public Tag createTag(String name, Tag parent) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome del tag non può essere vuoto");
        }

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Tag newTag = new Tag();
            newTag.setName(name);

            if (parent != null) {
                Tag managedParent = em.find(Tag.class, parent.getId());
                if (managedParent == null) {
                    throw new IllegalArgumentException("Il tag genitore selezionato non è stato trovato");
                }
                managedParent.addChild(newTag);
            }

            em.persist(newTag);
            em.getTransaction().commit();

            return newTag;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Impossibile creare il tag: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Elimina un tag gestendo la transazione manualmente.
     *
     * @param id l'id del tag da eliminare
     */
    public void deleteTag(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Tag tagToDelete = em.find(Tag.class, id);
            if (tagToDelete == null) {
                throw new IllegalArgumentException("Impossibile eliminare tag con id " + id + ": non trovato");
            }
            em.remove(tagToDelete);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Impossibile eliminare il tag: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Trova il tag radice di una gerarchia partendo da un tag qualsiasi.
     *
     * @param tag il tag da cui iniziare la ricerca
     * @return il tag genitore di livello più alto (radice)
     */
    public Tag findRootTag(Tag tag) {
        if (tag == null) return null;
        Tag current = tag;
        while (current.getParentTag() != null) {
            current = current.getParentTag();
        }
        return current;
    }

    /**
     * Costruisce la struttura ad albero dei tag per l'interfaccia grafica.
     *
     * @return un TreeItem radice che contiene tutta la gerarchia dei tag
     */
    public TreeItem<Tag> getTagTree() {
        List<Tag> allTags = this.getAllTags();
        Map<Long, TreeItem<Tag>> treeItemMap = new HashMap<>();
        TreeItem<Tag> root = new TreeItem<>();
        root.setExpanded(true);

        allTags.forEach(tag -> treeItemMap.put(tag.getId(), new TreeItem<>(tag)));

        allTags.forEach(tag -> {
            TreeItem<Tag> currentItem = treeItemMap.get(tag.getId());
            if (tag.getParentTag() != null) {
                TreeItem<Tag> parentItem = treeItemMap.get(tag.getParentTag().getId());
                if (parentItem != null) {
                    parentItem.getChildren().add(currentItem);
                }
            } else {
                root.getChildren().add(currentItem);
            }
        });

        return root;
    }
}
