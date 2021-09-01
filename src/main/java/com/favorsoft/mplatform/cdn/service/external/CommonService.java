package com.favorsoft.mplatform.cdn.service.external;

import java.util.List;

public interface CommonService<T> {
    List<T> getList();
    T save(T entity);
    T getObject(Object key);
    void delete(Object key);
}
