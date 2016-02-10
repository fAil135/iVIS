package com.imcode.entities.observer;

import com.imcode.entities.superclasses.AbstractIdEntity;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * This represents an Event to be Logged
 *
 *
 * @author Gonto
 * @since Dec 11, 2012
 */
@Entity
public class Event extends AbstractIdEntity<Long> {

    @Transient
    public transient Model model;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date timestamp;

    @Column(nullable = false)
    public String modelName;

    @Column(nullable = false)
    public Long modelId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public Action action;

    @Column
    public String fieldName;

    @Column
    public String previousValue;

    @Column
    public String newValue;

    @Column
    public Long userId;

    @Column
    public Long accountId;

    public Event(Model model, Action action, String fieldName, String previousValue, String newValue,
            Long userId, Long accountId) {
        this.model = model;
        this.modelName = model.getClass().getSimpleName();
        this.timestamp = new Date();
        this.action = action;
        this.modelId = model.id;
        this.fieldName = fieldName;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.userId = userId;
        this.accountId = accountId;
    }






    public static enum Action {
        CREATE,
        MODIFY,
        DELETE
    }

}
