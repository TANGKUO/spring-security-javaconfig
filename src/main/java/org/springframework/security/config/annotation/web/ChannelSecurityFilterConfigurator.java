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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.access.channel.ChannelDecisionManagerImpl;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.access.channel.ChannelProcessor;
import org.springframework.security.web.access.channel.InsecureChannelProcessor;
import org.springframework.security.web.access.channel.RetryWithHttpEntryPoint;
import org.springframework.security.web.access.channel.RetryWithHttpsEntryPoint;
import org.springframework.security.web.access.channel.SecureChannelProcessor;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.RequestMatcher;

/**
 * Adds channel security (i.e. requires HTTPS or HTTP) to an application. In order for
 * {@link ChannelSecurityFilterConfigurator} to be useful, at least one {@link RequestMatcher} should be mapped to HTTP
 * or HTTPS.
 *
 * <p>
 * By default an {@link InsecureChannelProcessor} and a {@link SecureChannelProcessor} will be registered.
 * </p>
 *
 * <h2>Security Filters</h2>
 *
 * The following Filters are populated
 *
 * <ul>
 *     <li>{@link ChannelProcessingFilter}</li>
 * </ul>
 *
 * <h2>Shared Objects Created</h2>
 *
 * No shared objects are created.
 *
 * <h2>Shared Objects Used</h2>
 *
 * The following shared objects are used:
 *
 * <ul>
 *     <li>{@link PortMapper} is used to create the default {@link ChannelProcessor} instances</li>
 * </ul>
 *
 * @author Rob Winch
 * @since 3.2
 */
public final class ChannelSecurityFilterConfigurator extends
        BaseRequestMatcherRegistry<ChannelSecurityFilterConfigurator.AuthorizedUrl,DefaultSecurityFilterChain,HttpConfiguration> {
    private ChannelProcessingFilter channelFilter = new ChannelProcessingFilter();
    private LinkedHashMap<RequestMatcher,Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestMatcher,Collection<ConfigAttribute>>();
    private List<ChannelProcessor> channelProcessors;

    /**
     * Creates a new instance
     * @see HttpConfiguration#requiresChannel()
     */
    ChannelSecurityFilterConfigurator() {
    }

    @Override
    public void configure(HttpConfiguration http) throws Exception {
        ChannelDecisionManagerImpl channelDecisionManager = new ChannelDecisionManagerImpl();
        channelDecisionManager.setChannelProcessors(getChannelProcessors(http));
        channelFilter.setChannelDecisionManager(channelDecisionManager);

        DefaultFilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource =
                new DefaultFilterInvocationSecurityMetadataSource(requestMap);
        channelFilter.setSecurityMetadataSource(filterInvocationSecurityMetadataSource);

        http.addFilter(channelFilter);
    }


    private List<ChannelProcessor> getChannelProcessors(HttpConfiguration http) {
        if(channelProcessors != null) {
            return channelProcessors;
        }

        InsecureChannelProcessor insecureChannelProcessor = new InsecureChannelProcessor();
        SecureChannelProcessor secureChannelProcessor = new SecureChannelProcessor();

        PortMapper portMapper = http.getSharedObject(PortMapper.class);
        if(portMapper != null) {
            RetryWithHttpEntryPoint httpEntryPoint = new RetryWithHttpEntryPoint();
            httpEntryPoint.setPortMapper(portMapper);
            insecureChannelProcessor.setEntryPoint(httpEntryPoint);

            RetryWithHttpsEntryPoint httpsEntryPoint = new RetryWithHttpsEntryPoint();
            httpsEntryPoint.setPortMapper(portMapper);
            secureChannelProcessor.setEntryPoint(httpsEntryPoint);
        }
        return Arrays.<ChannelProcessor>asList(insecureChannelProcessor, secureChannelProcessor);
    }


    private ChannelSecurityFilterConfigurator addAttribute(String attribute, List<RequestMatcher> matchers) {
        for(RequestMatcher matcher : matchers) {
            Collection<ConfigAttribute> attrs = Arrays.<ConfigAttribute>asList(new SecurityConfig(attribute));
            requestMap.put(matcher, attrs);
        }
        return this;
    }

    @Override
    AuthorizedUrl chainRequestMatchers(List<RequestMatcher> requestMatchers) {
        return new AuthorizedUrl(requestMatchers);
    }

    public class AuthorizedUrl {
        private List<RequestMatcher> requestMatchers;

        private AuthorizedUrl(List<RequestMatcher> requestMatchers) {
            this.requestMatchers = requestMatchers;
        }

        public ChannelSecurityFilterConfigurator requiresSecure() {
            return requires("REQUIRES_SECURE_CHANNEL");
        }

        public ChannelSecurityFilterConfigurator requiresInsecure() {
            return requires("REQUIRES_INSECURE_CHANNEL");
        }

        public ChannelSecurityFilterConfigurator requires(String attribute) {
            return addAttribute(attribute, requestMatchers);
        }
    }
}