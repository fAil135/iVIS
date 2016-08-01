package com.imcode.entities;

import com.imcode.entities.superclasses.AbstractIdEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by ruslan on 01.08.16.
 */
@Entity
@Table(name = "dbo_entity_rest_provider_information")
public class EntityRestProviderInformation extends AbstractIdEntity<Long> implements Serializable{

    @Column(name = "entity_class")
    private String entityClass;

    @Column(name = "rest_controller_class")
    private Class<?> restControllerClass;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "entityRestProviderInfo")
    private Set<MethodRestProviderForEntity> entityProviderMethods;


}
