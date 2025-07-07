package pengelolapasien.util;

import java.util.List;

public interface GenericDAO<T, K> {

    void insert(T obj) throws Exception;

    void update(T obj) throws Exception;

    void delete(K id) throws Exception;

    T findById(K id) throws Exception;

    List<T> findAll() throws Exception;
}
