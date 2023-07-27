package edu.upc.essi.dtim.odin.NextiaStore.RelationalStore;

import java.util.List;

public interface ORMStoreInterface {
    /**
     * Saves the object in the relational store.
     *
     * @param object The object to save.
     * @return The saved object.
     */
    <T> T save(T object);

    /**
     * Finds an object by its ID in the relational store.
     *
     * @param entityClass The class of the object to find.
     * @param id          The ID of the object to find.
     * @return The found object, or null if not found.
     */
    <T> T findById(Class<T> entityClass, String id);

    /**
     * Retrieves all objects of the specified class from the relational store.
     *
     * @param entityClass The class of the objects to retrieve.
     * @return A list of all objects of the specified class.
     */
    <T> List<T> getAll(Class<T> entityClass);

    /**
     * Deletes an object with the specified ID from the relational store.
     *
     * @param id The ID of the object to delete.
     * @return true if the object was successfully deleted, false otherwise.
     */
    <T> boolean deleteOne(Class<T> entityClass, String id);
}
