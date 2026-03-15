package edu.cit.camoro.peertayo.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Wraps {@link DefaultOAuth2AuthorizationRequestResolver} and adds
 * {@code prompt=select_account} to every Google authorization request so that
 * Google always shows the account-picker screen instead of silently reusing
 * the last authenticated session.
 */
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String REGISTRATION_BASE_URI = "/oauth2/authorization";

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(repo, REGISTRATION_BASE_URI);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customize(req);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return customize(req);
    }

    /**
     * Adds {@code prompt=select_account} so Google always shows the
     * account-selection screen, regardless of any existing Google session.
     */
    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req) {
        if (req == null) {
            return null;
        }
        return OAuth2AuthorizationRequest.from(req)
                .additionalParameters(params -> params.put("prompt", "select_account"))
                .build();
    }
}
