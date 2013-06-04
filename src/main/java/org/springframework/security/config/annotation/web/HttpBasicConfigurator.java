/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.config.annotation.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfiguratorAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Adds HTTP basic based authentication. All attributes have reasonable defaults
 * making all parameters are optional.
 *
 * <h2>Security Filters</h2>
 *
 * The following Filters are populated
 *
 * <ul>
 * <li>
 * {@link BasicAuthenticationFilter}
 * </li>
 * </ul>
 *
 * <h2>Shared Objects Created</h2>
 *
 * No shared objects are populated
 *
 * <h2>Shared Objects Used</h2>
 *
 * The following shared objects are used:
 *
 * <ul>
 * <li>{@link HttpConfiguration#authenticationManager()} </li>
 * </ul>
 *
 * @author Rob Winch
 * @since 3.2
 */
public final class HttpBasicConfigurator extends SecurityConfiguratorAdapter<DefaultSecurityFilterChain,HttpConfiguration> {
    private static final String DEFAULT_REALM = "Spring Security Application";

    private BasicAuthenticationFilter basicAuthenticationFilter;
    private AuthenticationEntryPoint authenticationEntryPoint;
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;

    /**
     * Creates a new instance
     * @throws Exception
     */
    public HttpBasicConfigurator() throws Exception {
        realmName(DEFAULT_REALM);
    }

    /**
     * Shortcut for {@link #authenticationEntryPoint(AuthenticationEntryPoint)}
     * specifying a {@link BasicAuthenticationEntryPoint} with the specified
     * realm name.
     *
     * @param realmName
     *            the HTTP Basic realm to use
     * @return {@link HttpBasicConfigurator} for additional customization
     * @throws Exception
     */
    public HttpBasicConfigurator realmName(String realmName) throws Exception {
        BasicAuthenticationEntryPoint basicAuthEntryPoint = new BasicAuthenticationEntryPoint();
        basicAuthEntryPoint.setRealmName(realmName);
        basicAuthEntryPoint.afterPropertiesSet();
        return authenticationEntryPoint(basicAuthEntryPoint);
    }

    /**
     * The {@link AuthenticationEntryPoint} to be ppulated on
     * {@link BasicAuthenticationFilter} in the event that authentication fails.
     * The default to use {@link BasicAuthenticationEntryPoint} with the realm
     * "Spring Security Application".
     *
     * @param authenticationEntryPoint the {@link AuthenticationEntryPoint} to use
     * @return {@link HttpBasicConfigurator} for additional customization
     */
    public HttpBasicConfigurator authenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        return this;
    }

    /**
     * Specifies a custom {@link AuthenticationDetailsSource} to use for basic
     * authentication. The default is {@link WebAuthenticationDetailsSource}.
     *
     * @param authenticationDetailsSource
     *            the custom {@link AuthenticationDetailsSource} to use
     * @return {@link HttpBasicConfigurator} for additional customization
     */
    public HttpBasicConfigurator authenticationDetailsSource(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.authenticationDetailsSource = authenticationDetailsSource;
        return this;
    }

    @Override
    public void configure(HttpConfiguration http) throws Exception {
        AuthenticationManager authenticationManager = http.authenticationManager();
        basicAuthenticationFilter = new BasicAuthenticationFilter(authenticationManager, authenticationEntryPoint);
        if(authenticationDetailsSource != null) {
            basicAuthenticationFilter.setAuthenticationDetailsSource(authenticationDetailsSource);
        }
        basicAuthenticationFilter.afterPropertiesSet();
        http.addFilter(basicAuthenticationFilter);
    }
}
