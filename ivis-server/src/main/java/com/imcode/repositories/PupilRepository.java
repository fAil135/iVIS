package com.imcode.repositories;

import com.imcode.entities.Pupil;

/**
 * Created by vitaly on 13.05.15.
 */
public interface PupilRepository extends PersonalizedRepository<Pupil>{
//    @Query("select p from Pupil p where p.person.personalId = :personalId")
//    Pupil findByPersonalId(@Param("personalId") String personalId);
}
