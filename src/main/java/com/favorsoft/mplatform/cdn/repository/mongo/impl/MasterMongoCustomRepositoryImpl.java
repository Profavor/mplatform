package com.favorsoft.mplatform.cdn.repository.mongo.impl;

import com.favorsoft.mplatform.cdn.dto.MasterDTO;
import com.favorsoft.mplatform.cdn.repository.mongo.MasterMongoCustomRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository("masterMongoCustomRepository")
public class MasterMongoCustomRepositoryImpl implements MasterMongoCustomRepository {

    private MongoTemplate mongoTemplate;

    public MasterMongoCustomRepositoryImpl(MongoTemplate mongoTemplate){
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void create(String domainId) {
        mongoTemplate.createCollection(domainId);
    }

    @Override
    public boolean collectionExists(String domainId) {
        return mongoTemplate.collectionExists(domainId);
    }

    @Override
    public MasterDTO masterIdExists(MasterDTO masterDTO){
        Query query = new Query(Criteria.where("masterId").is(masterDTO.getMasterId()));
        return mongoTemplate.findOne(query, MasterDTO.class, masterDTO.getDomainId());
    }

    @Override
    public MasterDTO insert(MasterDTO masterDTO) {
        return mongoTemplate.insert(masterDTO, masterDTO.getDomainId());
    }

    @Override
    public void update(MasterDTO masterDTO) {
        Query query = Query.query(Criteria.where("masterId").is(masterDTO.getMasterId()));
        mongoTemplate.findAndReplace(query, masterDTO, masterDTO.getDomainId());
    }

    @Override
    public void drop(String domainId) {
        mongoTemplate.dropCollection(domainId);
    }
}
