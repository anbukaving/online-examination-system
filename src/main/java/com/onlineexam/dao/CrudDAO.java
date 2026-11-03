package com.onlineexam.dao;

import java.util.List;

// ABSTRACTION + GENERICS: a contract that every DAO must follow.
public interface CrudDAO<T> {
    boolean add(T item);        // Create
    List<T> getAll();           // Read
    boolean update(T item);     // Update
    boolean delete(int id);     // Delete
}