package com.imcode.controllers.restful;

import com.imcode.controllers.AbstractRestController;
import com.imcode.entities.Incident;
import com.imcode.entities.Issue;
import com.imcode.entities.Status;
import com.imcode.exceptions.MessagingException;
import com.imcode.services.IncidentService;
import com.imcode.services.StatusService;
import com.imcode.services.UserService;
import com.imcode.utils.StaticUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;


import java.util.Date;
import java.util.List;

/**
 * Created by ruslan on 5/4/16.
 */
@RestController
@RequestMapping("/v1/{format}/incidents")
public class IncidentRestControllerImpl extends AbstractRestController<Incident, Long, IncidentService> {

    @Autowired
    IncidentService incidentService;

    @Autowired
    UserService userService;

    @Autowired
    StatusService statusService;

    @Override
    public Object create(@Validated @RequestBody Incident entity, WebRequest webRequest) throws MessagingException {
        try {
            entity.setReportDay(new Date());
            entity.setReportedBy(StaticUtils.getCurrentUser(webRequest, userService).getPerson());
            List<Status> statuses = statusService.findAll();
            entity.setStatus(statuses.stream().filter(status -> status.getName().equals(Status.State.NEW)).findFirst().get());
            return super.create(entity, webRequest);
        } catch (Exception ex) {
            throw new MessagingException("Requested values conflict with DB", ex.getMessage());
        }

    }

    @RequestMapping(method = RequestMethod.GET, params = {"search_text", "order_by"})
    public Object findByCriteria (@RequestParam(value = "search_text") String searchText,
                                  @RequestParam(value = "order_by") String orderBy,
                                  WebRequest webRequest) {

        if (orderBy.equals("title")) {
            return incidentService.findBySearchCriteria(searchText, orderBy);
        }

        return null;
    }

    @Override
    public Object update(@PathVariable("id") Long aLong, @RequestBody(required = false) Incident entity, WebRequest webRequest) {

        return super.update(aLong, entity, webRequest);
    }


}
