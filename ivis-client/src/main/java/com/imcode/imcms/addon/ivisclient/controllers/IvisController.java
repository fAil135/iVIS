package com.imcode.imcms.addon.ivisclient.controllers;

import com.imcode.entities.Person;
import com.imcode.entities.Pupil;
import com.imcode.entities.Statement;
import com.imcode.entities.enums.StatementStatus;
import com.imcode.imcms.addon.ivisclient.controllers.form.Message;
import com.imcode.imcms.addon.ivisclient.controllers.form.MessageType;
import com.imcode.services.PupilService;
import com.imcode.services.StatementService;
import imcode.services.restful.IvisFacade;
import imcode.services.restful.IvisServiceFactory;
import imcode.services.utils.IvisOAuth2Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedList;

/**
 * Created by vitaly on 26.05.15.
 */
@Controller
@RequestMapping(value = "/ivis")
public class IvisController {
    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private OAuth2ProtectedResourceDetails client;


    public static final String GRANT_TYPE = "authorization_code";
    //    todo Заинжектить значение мз placeholder
    @Value("${AuthorizationCodeHandlerUri}")
    public String tokenHandler;// = Imcms.getServerProperties().getProperty("AuthorizationCodeHandlerUri");

    @Value("${StatementsAddress}")
    private String statementsAddress;

    @Value("${ServerAddress}")
    private String serverAddress;

    @Value("${ClientAddress}")
    private String clientAddress;


    @RequestMapping(value = "/code")
    @ResponseBody
    public String getCode(WebRequest webRequest,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          @RequestParam(value = "code", required = false) String code,
                          @RequestParam(value = "redirect_uri", required = false) String redirectUri
    ) throws URISyntaxException, IOException {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", GRANT_TYPE);
        form.add("code", code);
        form.add("redirect_uri", tokenHandler);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization",
                String.format("Basic %s", new String(Base64.encode(String.format("%s:%s", client.getClientId(), client.getClientSecret()).getBytes("UTF-8")), "UTF-8")));
        HttpEntity httpEntity = new HttpEntity(form, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<OAuth2AccessToken> result = restTemplate.postForEntity(client.getAccessTokenUri(), httpEntity, OAuth2AccessToken.class);

        IvisOAuth2Utils.setAccessToken(request, result.getBody());
//        response.sendRedirect(statementsAddress);
        response.sendRedirect(clientAddress + "/show.jsp");

        return result.getBody().toString();
    }

    @RequestMapping(value = "/token")
    @ResponseBody
    public String getToken(HttpServletResponse response) throws IOException {
        String s = "sdfas";
        response.sendRedirect(clientAddress + "/show.jsp");
        return s;
    }

    @RequestMapping(value = "/{id}", params = {"status"}, method = RequestMethod.POST)
    @ResponseBody
    public String updateStatus(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable("id") Long id, @RequestParam("status") StatementStatus status) throws IOException {
        IvisFacade ivis = IvisFacade.instance(new IvisFacade.Configuration.Builder()
                .endPointUrl(serverAddress)
                .responseType("json")
                .version("v1").build());
        IvisServiceFactory factory = ivis.getServiceFactory(client, IvisOAuth2Utils.getClientContext(request));
        StatementService service = factory.getStatementService();

        if (IvisOAuth2Utils.getAccessToken(request) != null) {
            try {
                Statement statement = service.find(id);

                if (statement != null) {
                    statement.setStatus(status);

                    service.save(statement);

                }
            } catch (UserRedirectRequiredException e) {
                IvisOAuth2Utils.setAccessToken(request, null);
            }
        }

        response.sendRedirect(statementsAddress);

        return id + ":" + status.toString();
    }

    @RequestMapping(value = "/xml", method = RequestMethod.POST)
    public void importApplication(HttpServletRequest request,
                                    HttpServletResponse response,
                                  @RequestParam(value = "file", required = false) MultipartFile file,
//                                    @RequestParam("body") String body,
                                    Model model) throws IOException, URISyntaxException {

        InputStream inputStream = file.getInputStream();
        Statement statement = pharseXml(inputStream);

        if (statement == null) {
            throw new RuntimeException("Unknown xml format");
        }

        if (IvisOAuth2Utils.getAccessToken(request) != null) {

            IvisFacade ivis = IvisFacade.instance(new IvisFacade.Configuration.Builder()
                    .endPointUrl(serverAddress)
                    .responseType("json")
                    .version("v1").build());
            IvisServiceFactory factory = ivis.getServiceFactory(client, IvisOAuth2Utils.getClientContext(request));
            StatementService statementService = factory.getStatementService();
            PupilService pupilService = factory.getPupilService();

            try {
//                statement = new Statement();
//                statement.setStatus(StatementStatus.created);
                if (statement.getPupil() != null) {
                    Pupil pupil = pupilService.findByPersonalId(statement.getPupil().getPerson().getPersonalId());
                    statement.setPupil(pupil);
                }
                statementService.save(statement);
                model.asMap().clear();
                model.addAttribute("message", new Message(MessageType.SUCCESS, "SUCCESS"));
            } catch (Exception e) {
                model.addAttribute("message", new Message(MessageType.ERROR, "ERROR"));
            }
        } else {
            response.sendRedirect(IvisOAuth2Utils.getOAuth2AuthirizationUrl((AuthorizationCodeResourceDetails) client, tokenHandler));
            return;
        }

        response.sendRedirect(clientAddress + "/servlet/AdminDoc?meta_id=1005");
//        servlet/AdminDoc?meta_id=1005
//        return "xml/show";
    }

    @RequestMapping(value = "/xml", method = RequestMethod.GET)
    public String showImportApplicationForm() {

        return "xml/show";
    }

    private static Statement pharseXml(InputStream inputStream) {
        StatmentHandler handler = new StatmentHandler();

        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            saxParser.parse(inputStream, handler);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return handler.getStatement();
    }

   private static class StatmentHandler extends DefaultHandler {
        private Statement statement;
        private LinkedList<String> nodes = new LinkedList<>();
        private String fullElementName;
        private String elementName;


        @Override
        public void startDocument() throws SAXException {
            if (statement != null) {
                throw new SAXException("This statement handler " + this + " is allredy used, please create a new one.");
            }

            statement = new Statement();
            statement.setStatus(StatementStatus.created);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            nodes.add(qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String currentNode = nodes.getLast();
            String value = new String(ch, start, length);
            System.out.println(ch);

            if (nodes.contains("student") && "Personnummer".equalsIgnoreCase(currentNode)) {
                Person person = new Person(value, null, null);
                Pupil pupil = new Pupil();
                pupil.setPerson(person);
                statement.setPupil(pupil);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            nodes.removeLast();
        }

        public Statement getStatement() {
            return statement;
        }
    }
}