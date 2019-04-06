package com.dglozano.escale.db.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

public abstract class BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract Long insert(T entity);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract void update(T entity);

    @Delete
    public abstract void delete(T entity);

    public void upsert(T entity) {
        long id = insert(entity);
        if (id == -1) {
            update(entity);
        }
    }
}
