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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

@WebIntegrationTest({"server.port=0", "management.port=0"})
@SpringApplicationConfiguration(classes = {TestApplication.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class EndpointsMvcEndpointTest {

    @Autowired
    WebApplicationContext context;
    @Autowired
    ManagementServerProperties properties;
    @Value("${local.management.port}")
    int port;
    MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void should_expose_endpoints_listing() throws Exception {
        final String uri = "http://localhost:" + port + properties.getContextPath() + "/endpoints";

        final List<EndpointResource> resources = new RestTemplate().exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<List<EndpointResource>>() {}).getBody();

        //System.out.println(prettyPrint(resources));
        final Optional<EndpointResource> healthEndpoint = endpointWithId("health", resources);
        assertTrue(healthEndpoint.isPresent());
        assertTrue(healthEndpoint.get().getLink(Link.REL_SELF).getHref().contains("localhost:" + port + properties.getContextPath() + "/health"));
    }

    private Optional<EndpointResource> endpointWithId(final String id, final List<EndpointResource> o) {
        return o.stream().filter(e -> id.equals(e.endpoint.id)).findFirst();
    }

    private ObjectMapper mapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                                          .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                                          .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                          .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                                          .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
                                          .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    private String prettyPrint(final List<EndpointResource> o) throws JsonProcessingException {return mapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);}

    static class EndpointResource extends ResourceSupport {
        public Endpoint endpoint;
    }

    static class Endpoint {
        public String id;
        public Boolean sensitive;
        public Boolean enabled;
    }

}
