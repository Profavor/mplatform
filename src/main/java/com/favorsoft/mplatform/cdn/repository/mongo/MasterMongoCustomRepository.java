package com.favorsoft.mplatform.cdn.repository.mongo;

import com.favorsoft.mplatform.cdn.dto.MasterDTO;

public interface MasterMongoCustomRepository {

    void create(String domainId);

    boolean collectionExists(String domainId);

    MasterDTO insert(MasterDTO masterDTO);

    MasterDTO masterIdExists(MasterDTO masterDTO);

    void update(MasterDTO masterDTO);

    void drop(String domainId);
}
