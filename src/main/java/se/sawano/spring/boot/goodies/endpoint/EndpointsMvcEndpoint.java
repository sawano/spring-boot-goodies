/*
 * Copyright 2015 Daniel Sawano
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.sawano.spring.boot.goodies.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Exposes a list of all registered endpoints and their URLs.
 *
 * <p>Basic idea derived from http://blog.codeleak.pl/2014/10/spring-boot-actuator-custom-endpoint.html</p>
 */
@Component
public class EndpointsMvcEndpoint extends EndpointMvcAdapter {

    @Autowired
    public EndpointsMvcEndpoint(final EndpointsEndpoint delegate) {
        super(requireNonNull(delegate));
    }

    @Override
    public EndpointsEndpoint getDelegate() {
        return (EndpointsEndpoint) super.getDelegate();
    }

    @Override
    public Object invoke() {
        if (getDelegate().isEnabled()) {
            final UriComponents baseComponents = ServletUriComponentsBuilder.fromCurrentServletMapping().build();
            return getDelegate().invoke().stream()
                                .map(e -> new EndpointResource(e, baseComponents))
                                .collect(toList());
        }

        return super.invoke();
    }

    static class EndpointResource extends ResourceSupport {

        public final Endpoint endpoint;

        EndpointResource(Endpoint endpoint, final UriComponents baseComponents) {
            this.endpoint = endpoint;
            if (endpoint.isEnabled()) {
                add(selfLink(endpoint, baseComponents));
            }
        }

        private static Link selfLink(final Endpoint endpoint, final UriComponents baseComponents) {
            final UriComponents selfUriComponents = UriComponentsBuilder.newInstance()
                                                                        .uriComponents(baseComponents)
                                                                        .pathSegment(endpoint.getId())
                                                                        .build();
            return new Link(selfUriComponents.toUriString(), Link.REL_SELF);
        }
    }
}
