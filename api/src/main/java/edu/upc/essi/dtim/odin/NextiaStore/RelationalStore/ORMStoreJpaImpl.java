package edu.upc.essi.dtim.odin.NextiaStore.RelationalStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.List;

/**
 * Implementation of the {@link ORMStoreInterface} using Java Persistence API (JPA).
 */
public class ORMStoreJpaImpl implements ORMStoreInterface {

    private static final Logger logger = LoggerFactory.getLogger(ORMStoreJpaImpl.class);
    private final EntityManagerFactory emf;

    /**
     * Constructs a new instance of {@code OrmStoreJpaImpl}.
     * Initializes the entity manager factory using the "ORMPersistenceUnit" defined in the persistence configuration.
     */
    public ORMStoreJpaImpl() {
        emf = Persistence.createEntityManagerFactory("ORMPersistenceUnit");
    }

    /**
     * Saves or updates an entity in the database.
     *
     * @param object The entity to be saved or updated.
     * @return The saved or updated entity.
     */
    @Override
    public <T> T save(T object) {
        EntityManager em = emf.createEntityManager();
        T savedObject = null;
        try {
            em.getTransaction().begin();
            savedObject = em.merge(object);
            em.getTransaction().commit();
            logger.info("Object " + object.getClass() + " saved successfully");
        } catch (Exception e) {
            logger.error("Error saving object " + object.getClass() + " error: " + e.getMessage());
        } finally {
            em.close();
        }
        return savedObject;
    }

    /**
     * Retrieves an entity of the specified class by its unique identifier from the database.
     *
     * @param entityClass The class of the entity to retrieve.
     * @param id          The unique identifier of the entity.
     * @return The retrieved entity, or null if no entity with the specified ID is found.
     */
    @Override
    public <T> T findById(Class<T> entityClass, String id) {
        EntityManager em = emf.createEntityManager();
        T object = null;
        try {
            object = em.find(entityClass, id);
        } catch (Exception e) {
            logger.warn("Error finding object " + entityClass.getSimpleName() + " error: " + e.getMessage());
        } finally {
            em.close();
        }
        return object;
    }

    /**
     * Retrieves all entities of the specified class from the database.
     *
     * @param entityClass The class of the entities to retrieve.
     * @return A list containing all the retrieved entities, or an empty list if none are found.
     */
    @Override
    public <T> List<T> getAll(Class<T> entityClass) {
        EntityManager em = emf.createEntityManager();
        List<T> objects = null;
        try {
            String queryString = "SELECT d FROM " + entityClass.getSimpleName() + " d";
            em.getTransaction().begin();
            @SuppressWarnings("squid:S2077")
            Query query = em.createQuery(queryString);
            objects = query.getResultList();
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Error retrieving all objects " + entityClass.getSimpleName() + " error: " + e.getMessage());
        } finally {
            em.close();
        }
        return objects;
    }

    /**
     * Deletes an entity of the specified class by its identifier.
     *
     * @param entityClass The class of the entity to delete.
     * @param id          The identifier of the entity to delete.
     * @return {@code true} if the entity was successfully deleted, {@code false} otherwise.
     */
    @Override
    public <T> boolean deleteOne(Class<T> entityClass, String id) {
        EntityManager em = emf.createEntityManager();
        boolean success = false;
        try {
            em.getTransaction().begin();

            T objectToRemove = em.find(entityClass, id);
            if (objectToRemove != null) {
                logger.info(entityClass.getSimpleName() + " deleted");
                em.remove(objectToRemove);
                em.getTransaction().commit();
                success = true;
            } else {
                logger.warn("Error deleting " + entityClass.getSimpleName() + ". Object not found");
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            logger.error("Error deleting " + entityClass.getSimpleName() + " error: " + e.getMessage());
        } finally {
            em.close();
        }
        return success;
    }
}
