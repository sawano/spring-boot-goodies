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
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * Exposes a list of all registered endpoints.
 */
@Component
@ConfigurationProperties(prefix = "endpoints.endpoints", ignoreUnknownFields = true)
public class EndpointsEndpoint extends AbstractEndpoint<List<Endpoint>> {

    private final List<Endpoint> endpoints;

    @Autowired
    public EndpointsEndpoint(final List<Endpoint> endpoints) {
        super("endpoints", false, true);
        requireNonNull(endpoints);
        this.endpoints = endpoints.stream()
                                  .sorted(comparing(Endpoint::getId))
                                  .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @Override
    public List<Endpoint> invoke() {
        return endpoints;
    }

}

