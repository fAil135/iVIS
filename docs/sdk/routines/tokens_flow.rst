Tokens flow
===========

Prerequisites
-------------

    * `Login <http://docs.ivis.se/en/latest/sdk/routines/login.html>`_

Need say few words how to use tokens flow.

After login user in way described at `Login <http://docs.ivis.se/en/latest/sdk/routines/login.html>`_
in session placed
`access token <http://docs.spring.io/spring-security/oauth/apidocs/org/springframework/security/oauth2/common/OAuth2AccessToken.html>`_.
And also refresh token value from access token object put in cookie.

.. important::

    Cookie has expiration time defined. It is defined by value refresh token validity seconds,
    contact system administrator to know that.

So tokens flow looks like

    #. Client app login user (access token -> session, refresh token -> cookie with with expiration time).
    #. If token is expired (IvisOAuth2Utils.isTokenGood(httpServletRequest) -> exchange refresh token from cookie (cookie key "refreshToken") to access token.
    #. If cookie does not exist -> login user again.
