package com.imcode.security.vote;

import com.imcode.entities.MethodRestProviderForEntity;
import com.imcode.entities.User;
import com.imcode.entities.oauth2.JpaClientDetails;
import com.imcode.oauth2.IvisClientDetailsService;
import com.imcode.services.MethodRestProviderForEntityService;
import com.imcode.services.jpa.ClientDetailsServiceRepoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.exceptions.InsufficientScopeException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.web.FilterInvocation;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This voter checks scope in request is consistent with that held by the client. If there is no user in the request
 * (client_credentials grant) it checks against authorities of client instead of scopes by default. Activate by adding
 * <code>CLIENT_HAS_SCOPE</code> to security attributes.
 *
 * @author Dave Syer
 *
 */
public class ClientUserAllowedMethodVoter implements AccessDecisionVoter<Object> {

    private String clientHasScope = "CLIENT_HAS_SCOPE";

    private boolean throwException = true;

    @Autowired
    @Qualifier("clientDetailsServiceRepoImpl")
    private IvisClientDetailsService clientDetailsService;

    @Autowired
    private MethodRestProviderForEntityService methodRestProviderForEntityService;

    private boolean clientAuthoritiesAreScopes = true;

    /**
     * ClientDetailsService for looking up clients by ID.
     *
     * @param clientDetailsService the client details service (mandatory)
     */
    public void setClientDetailsService(ClientDetailsService clientDetailsService) {
        this.clientDetailsService = (IvisClientDetailsService) clientDetailsService;
    }

    /**
     * Flag to determine the behaviour on access denied. If set then we throw an {@link InsufficientScopeException}
     * instead of returning {@link AccessDecisionVoter#ACCESS_DENIED}. This is unconventional for an access decision
     * voter because it vetos the other voters in the chain, but it enables us to pass a message to the caller with
     * information about the required scope.
     *
     * @param throwException the flag to set (default true)
     */
    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    /**
     * Flag to signal that when there is no user authentication client authorities are to be treated as scopes.
     *
     * @param clientAuthoritiesAreScopes the flag value (default true)
     */
    public void setClientAuthoritiesAreScopes(boolean clientAuthoritiesAreScopes) {
        this.clientAuthoritiesAreScopes = clientAuthoritiesAreScopes;
    }

    /**
     * The name of the config attribute that can be used to deny access to OAuth2 client. Defaults to
     * <code>DENY_OAUTH</code>.
     *
     * @param denyAccess the deny access attribute value to set
     */
    public void setDenyAccess(String denyAccess) {
        this.clientHasScope = denyAccess;
    }

    public boolean supports(ConfigAttribute attribute) {
        if (clientHasScope.equals(attribute.getAttribute())) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * This implementation supports any type of class, because it does not query the presented secure object.
     *
     * @param clazz the secure object
     *
     * @return always <code>true</code>
     */
    public boolean supports(Class<?> clazz) {
        return true;
    }

    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {

        int result = ACCESS_ABSTAIN;

//        FilterInvocation fi = (FilterInvocation) object;
//        String requestUrl = fi.getRequestUrl();
//
//        if (!(requestUrl.matches("/api/v1/(json|xml)/(.*)"))) {
//            return result;
//        }
//
//        if (!(authentication instanceof OAuth2Authentication)) {
//            return result;
//        }
//
//        HttpServletRequest httpRequest = fi.getHttpRequest();
//
//        requestUrl = requestUrl.replaceFirst("/api/v1/(json|xml)/", "/api/v1/{format}/");
//        requestUrl = requestUrl.replaceFirst("/\\d+", "/{id}");
//        RequestMethod requestMethod = RequestMethod.valueOf(httpRequest.getMethod());
//
//        List<MethodRestProviderForEntity> byUrlAndRequestMethod = methodRestProviderForEntityService.findByUrlAndRequestMethod(
//                requestUrl.contains("?") ? requestUrl.substring(0, requestUrl.indexOf("?")) : requestUrl, requestMethod
//        );
//
//        Set httpParameters = httpRequest.getParameterMap().keySet();
//        Set keySet = httpRequest.getParameterMap().keySet();
//        byUrlAndRequestMethod.stream().filter(methodRestProviderForEntity ->
//            methodRestProviderForEntity.getInParameters().keySet().stream().allMatch(keySet::contains));
//
//        OAuth2Authentication oauth2Authentication = (OAuth2Authentication) authentication;
//        OAuth2Request clientAuthentication = oauth2Authentication.getOAuth2Request();
//        String clientId = clientAuthentication.getClientId();





//        JpaClientDetails client = (JpaClientDetails) clientDetailsService.loadClientByClientId(clientAuthentication.getClientId());
//        Set<MethodRestProviderForEntity> availableMethodsForClient = availableMethodsForClient(client);
//
//        result = ACCESS_GRANTED;//ACCESS_DENIED;
//
//        if (availableMethodsForClient.isEmpty()) {
//            return result;
//        }
//
//        result = ACCESS_GRANTED;//ACCESS_GRANTED;
//
//        if (availableMethodsForClient.stream().anyMatch(methodRestProviderForEntity ->
//                isMethodMatch(methodRestProviderForEntity, requestUrl, httpRequest))) {
//            return result;
//        }
//
//        result = ACCESS_GRANTED;//ACCESS_DENIED;
//
        return result;
    }

//    private Set<MethodRestProviderForEntity> availableMethodsForClient(JpaClientDetails clientDetails) {
//        Set<MethodRestProviderForEntity> allowedMethodsForClient = clientDetails.getAllowedMethods();
//        Set<MethodRestProviderForEntity> allowedMethodsForOwner = clientDetails.getOwner().getAllowedMethods();
//        allowedMethodsForClient.retainAll(allowedMethodsForOwner);
//        return allowedMethodsForClient;
//    }
//
//    private boolean isMethodMatch(MethodRestProviderForEntity methodRestProviderForEntity, String urlCheck, HttpServletRequest request) {
//
//        String patternForUrl = methodRestProviderForEntity.getUrl().replaceFirst("\\{format\\}", "(xml|json)");
//
//        if (patternForUrl.matches("(.*)\\{(\\w+)\\}(.*)")) {
//            patternForUrl = patternForUrl.replaceFirst("\\{id\\}", "^[1-9]\\d*$");
//        }
//
//        String methodCheck = request.getMethod();
//        String requestMethod = methodRestProviderForEntity.getRequestMethod().toString();
//
//        Set<String> inParameters = methodRestProviderForEntity.getInParameters().keySet();
//
//        inParameters.remove("id");
//
//        return urlCheck.matches(patternForUrl)
//                && methodCheck.equals(requestMethod)
//                && (inParameters.isEmpty() ? true : inParameters.stream().allMatch(parameter -> request.getAttribute(parameter) != null));
//
//    }

}
