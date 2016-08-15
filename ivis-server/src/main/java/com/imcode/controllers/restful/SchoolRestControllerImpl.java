package com.imcode.controllers.restful;

import com.imcode.controllers.AbstractRestController;
import com.imcode.entities.School;
import com.imcode.services.SchoolService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
@RequestMapping("/v1/{format}/schools")
public class SchoolRestControllerImpl extends AbstractRestController<School, Long, SchoolService> {

    @Override
    @RequestMapping(method = RequestMethod.GET, params = {"name"})
    public Object getByName(WebRequest webRequest, Model model, @RequestParam("name") String name, @RequestParam(value = "first", required = false) Boolean firstOnly) {
        return super.getByName(webRequest, model, name, firstOnly);
    }
}
