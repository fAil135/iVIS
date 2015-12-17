package com.imcode.services;

import com.imcode.MainTest;
import com.imcode.entities.Guardian;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

/**
 * Created by vitaly on 17.12.15.
 */
@Deprecated
@Service
@PersistenceContext
//@Transactional
public class TestService {
    @Autowired
    private EntityManager em;
    @Autowired
    private GuardianService guardianService;

    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

    public GuardianService getGuardianService() {
        return guardianService;
    }

    public void setGuardianService(GuardianService guardianService) {
        this.guardianService = guardianService;
    }

//    @Transactional
    public void test() {
        Guardian g1 = MainTest.getPersonalizedEntity(Guardian::new, "790411-5867", "Birgit", "Engström");
        Guardian g2 = MainTest.getPersonalizedEntity(Guardian::new, "530304-5677", "Orvar", "Vestman");
        em.merge(g1);
        em.persist(g2);
        System.out.println();
    }

    @Transactional
    public void flush() {
        em.flush();
    }

    public void merge(Collection<?> entities) {
        entities.forEach(em::merge);
    }

    @Transactional
    public void mergeTransactional(Collection<?> entities) {
        merge(entities);
        em.flush();
    }

}
