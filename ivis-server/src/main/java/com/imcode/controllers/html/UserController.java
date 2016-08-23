package com.imcode.controllers.html;

import com.imcode.controllers.html.exceptions.NotFoundException;
import com.imcode.entities.MethodRestProviderForEntity;
import com.imcode.entities.Role;
import com.imcode.entities.User;
import com.imcode.entities.enums.CommunicationTypeEnum;
import com.imcode.services.EntityRestProviderInformationService;
import com.imcode.services.MethodRestProviderForEntityService;
import com.imcode.services.RoleService;
import com.imcode.services.UserService;
import com.imcode.utils.CollectionTransferUtil;
import com.imcode.utils.MailSenderUtil;
import com.imcode.utils.StaticUtls;
import com.imcode.validators.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    MessageSource messageSource;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.smtp.from.address}")
    private String fromAddress;

    @Value("${mail.smtp.from.username}")
    private String fromUsername;

    @Autowired
    private EntityRestProviderInformationService entityRestProviderInformationService;

    @Autowired
    private MethodRestProviderForEntityService methodRestProviderForEntityService;
    //    Shows the list of users
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView list(ModelAndView model, Authentication authentication) {
        model.setViewName("users/list");
        model.addObject(userService.findAll());

        return model;
    }

    //    Show the UPDATE form
    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public ModelAndView updateForm(@PathVariable("id") User user,
                                   ModelAndView model,
                                   WebRequest webRequest,
                                   Locale locale) {

        User currentUser = StaticUtls.getCurrentUser(webRequest, userService);
        if(!currentUser.hasRoles("ROLE_ADMIN")) {
            if (!user.getId().equals(currentUser.getId())) {
                model.setViewName("redirect:/");
                return model;
            }
        }

        if (user == null) {
            model.setViewName("users/list");
            throw new NotFoundException();
//            model.addObject(new Message(MessageType.ERROR, messageSource.getMessage("entity.notFoundById", new Object[]{User.class.getSimpleName(), id}, locale)));
        }

        model.setViewName("users/edit");
        model.addObject(user);
        model.addObject(roleService.findAll());

        return model;
    }

    //    Show the CREATE form
    @RequestMapping(params = "form", method = RequestMethod.GET)
    public ModelAndView createForm(ModelAndView model) {
        model.setViewName("users/edit");
        model.addObject(roleService.findAll());
        User user = new User();
        Role roleUser = roleService.findFirstByName("ROLE_USER");
        model.addObject(user);

        if (roleUser != null) {
            user.setAuthorities(roleUser);
        }

        return model;
    }

    //    Show the PERMISSION form
    @RequestMapping(value = "/{id}", params = "perm", method = RequestMethod.GET)
    public ModelAndView permissionForm(@PathVariable("id") User user,
                                   ModelAndView model,
                                   WebRequest webRequest,
                                   Locale locale) {

        if (user == null) {
            model.setViewName("users/list");
            throw new NotFoundException();
        }

        model.addObject("identifier", user.getId());
        model.setViewName("users/permissions");
        model.addObject(methodRestProviderForEntityService.findAll());
        model.addObject(entityRestProviderInformationService.findAll());
        model.addObject("specify", "user");
        model.addObject("allowedMethods",
                new CollectionTransferUtil<>(methodRestProviderForEntityService.findAllowedMethodsByUserId(user.getId())));
        return model;
    }

    //    CREATE new user
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView create(@ModelAttribute("user") @Valid User user,
                               BindingResult bindingResultUser,
                               ModelAndView model,
                               WebRequest webRequest,
                               Locale locale) {

        if (webRequest.isUserInRole("ROLE_ADMIN")) {
            user.setConfirmPassword(user.getPassword());
        }

        ValidationUtils.invokeValidator(userValidator, user, bindingResultUser);

        if (userService.findByUsername(user.getUsername()) != null) {
            bindingResultUser.rejectValue("username", null, "User is alredy exists!");
        }

        if (bindingResultUser.hasErrors()) {
            model.addObject(user);
            model.setViewName("users/edit");

            return model;
        }

        StaticUtls.encodeUserPassword(user);

        userService.save(user);

        model.setViewName("redirect:/users");

        return model;
    }


//    @ModelAttribute("user")
//    public User bindUser(User user) {
//        Set<Role> roleSet = new HashSet<>();
//        Set<Role> roleNameSet = user.getAuthorities();
//
//        for (Role role :roleNameSet) {
//            Role persistRole = roleService.findByName(role.getName());
//            if (persistRole != null) {
//                roleSet.add(persistRole);
//            }
//        }
//
//        user.setAuthorities(roleSet);
//
//        return user;
//    }

        //    UPDATE exists user
        @RequestMapping(value = "/{id}", method = RequestMethod.POST)
        public ModelAndView update (@PathVariable("id") User persistUser,
                                    @ModelAttribute("user") @Valid User user,
                                    BindingResult bindingResultUser,
                                    ModelAndView model,
                                    WebRequest webRequest,
                                    Locale locale){

            if (webRequest.isUserInRole("ROLE_ADMIN")) {
                user.setConfirmPassword(user.getPassword());
            }

            ValidationUtils.invokeValidator(userValidator, user, bindingResultUser);

            if (bindingResultUser.hasErrors()) {
                model.addObject(user);
                model.setViewName("users/edit");

                return model;
            }

//            User persistUser = userService.find(id);

            if (persistUser == null) {
                throw new NotFoundException();
            }

//            String[] fieldExceptions = user.getPassword().isEmpty() ? new String[]{"id", "password", "confirmPassword"} : new String[]{"id"};
//
//            BeanUtils.copyProperties(user, persistUser, fieldExceptions);

            if (!user.getPassword().isEmpty()) {
                StaticUtls.encodeUserPassword(user);
            } else {
                user.setPassword(null);
                user.setConfirmPassword(null);
            }

            try {
                StaticUtls.nullAwareBeanCopy(persistUser, user);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            userService.save(persistUser);
            model.setViewName("redirect:/users");

            return model;
        }

    //    DELETE exists user
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("id") Long id) {
        if (!userService.exist(id)) {
            throw new NotFoundException();
        }

        userService.delete(id);

//        return "redirect:/users";
    }

    @RequestMapping(value = "/{id}", params = "passwordchange", method = RequestMethod.POST)
    public ModelAndView passwordChange(@PathVariable("id") User user,
                               @RequestParam("password") String password,
                               ModelAndView model)  {

        user.setPassword(password);
        StaticUtls.encodeUserPassword(user);

        userService.save(user);

        String to = user.getPerson().getEmails().get(CommunicationTypeEnum.HOME).getValue();
        String subject = "Change password in iVIS";
        String text = "Hello, " + user.getUsername() + ". Your password has bean changed.";

        MailSenderUtil mailSenderUtil = new MailSenderUtil(mailSender, false, false, fromAddress, fromUsername);
        mailSenderUtil.createMessage(to, subject, text);
        mailSenderUtil.sendMessage();

        model.setViewName("redirect:/logout.do");

        return model;
    }


    @RequestMapping(value = "/{id}", params = "checkpassword", method = RequestMethod.GET)
    public @ResponseBody Boolean checkPassword(@PathVariable("id") User user,
                                               @RequestParam("checkpassword") String password) {

        String userEncodedPassword = user.getPassword();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return encoder.matches(password, userEncodedPassword);

    }

    @RequestMapping(value = "/{id}", params = "permit", method = RequestMethod.POST)
    public ModelAndView permitMethods(@ModelAttribute("allowedMethods") CollectionTransferUtil<String> allowedMethods,
                                      @PathVariable("id") Long userId,
                                      ModelAndView model) {

        User user = userService.find(userId);
        Collection<String> idOfMethods = allowedMethods.getCollection();
        List<MethodRestProviderForEntity> allowedMethodsByUserId = methodRestProviderForEntityService.findAllowedMethodsByUserId(userId);

        allowedMethodsByUserId.stream()
                .peek(methodRestProviderForEntity -> methodRestProviderForEntity.deleteUser(userId))
                .forEach(methodRestProviderForEntityService::save);

        if (idOfMethods != null) {
            idOfMethods.stream()
                    .map(Long::parseLong)
                    .map(methodRestProviderForEntityService::find)
                    .peek(methodRestProviderForEntity -> methodRestProviderForEntity.addUser(user))
                    .forEach(methodRestProviderForEntityService::save);
        }

        model.setViewName("redirect:/users");
        return model;
    }



}

