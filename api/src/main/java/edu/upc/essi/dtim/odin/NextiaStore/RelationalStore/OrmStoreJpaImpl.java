package edu.upc.essi.dtim.odin.NextiaStore.RelationalStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.List;

public class OrmStoreJpaImpl implements ORMStoreInterface {

    private static final Logger logger = LoggerFactory.getLogger(OrmStoreJpaImpl.class);
    private final EntityManagerFactory emf;

    public OrmStoreJpaImpl() {
        emf = Persistence.createEntityManagerFactory("ORMPersistenceUnit");
    }

    @Override
    public <T> T save(T object) {
        EntityManager em = emf.createEntityManager();
        T savedObject = null;
        try {
            em.getTransaction().begin();
            savedObject = em.merge(object);
            em.getTransaction().commit();
            logger.info("Object {} saved successfully", object.getClass());
        } catch (Exception e) {
            logger.error("Error saving object {}: {}", object.getClass(), e.getMessage(), e);
        } finally {
            em.close();
        }
        return savedObject;
    }

    @Override
    public <T> T findById(Class<T> entityClass, String id) {
        EntityManager em = emf.createEntityManager();
        T object = null;
        try {
            object = em.find(entityClass, id);
        } catch (Exception e) {
            logger.warn("Error finding object {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
        } finally {
            em.close();
        }
        return object;
    }

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
            logger.error("Error retrieving all objects {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
        } finally {
            em.close();
        }
        return objects;
    }


    @Override
    public <T> boolean deleteOne(Class<T> entityClass, String id) {
        EntityManager em = emf.createEntityManager();
        boolean success = false;
        try {
            logger.info("-------------> STARTING DELETE PROCESS");
            em.getTransaction().begin();

            T objectToRemove = em.find(entityClass, id);
            if (objectToRemove != null) {
                logger.info("{} DELETED", entityClass.getSimpleName());
                em.remove(objectToRemove);
                em.getTransaction().commit();
                success = true;
            } else {
                logger.warn("Error deleting {}: Object not found", entityClass.getSimpleName());
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            logger.error("Error deleting {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
        } finally {
            em.close();
        }
        return success;
    }
}
