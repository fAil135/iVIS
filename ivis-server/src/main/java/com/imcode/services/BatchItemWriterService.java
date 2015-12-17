package com.imcode.services;

import com.imcode.entities.interfaces.JpaEntity;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by vitaly on 17.12.15.
 */
@Service
@PersistenceContext
//@Scope(value = "request")
//@Transactional
public class BatchItemWriterService implements ItemWriter<Object> {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Autowired
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    @Transactional
    public void write(List<?> items) throws Exception {
        merge((List<Object>) items);
        flush();
    }


    public void merge(List<Object> items) throws Exception {

        for (int i = 0; i < items.size(); i++) {
            Object entity = items.get(i);
            try {
                items.set(i, entityManager.merge(entity));
            } catch (Exception e) {
                logger.warning(e::getMessage);
            }
        }
    }

    public void flush() {
        entityManager.flush();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
