package jp.vcoin.gratuitybot.config;

import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@Slf4j
public class ApplicationConfig {

    private final ApplicationProperties applicationProperties;
    private final ClientHttpRequestInterceptor addCustomHeaderInterceptor;
    private final LoggingRequestInterceptor loggingRequestInterceptor;


    public ApplicationConfig(@Autowired ApplicationProperties applicationProperties,
                             @Autowired AddCustomHeaderInterceptor addCustomHeaderInterceptor,
                             @Autowired(required = false) LoggingRequestInterceptor loggingRequestInterceptor) {
        this.applicationProperties = applicationProperties;
        this.addCustomHeaderInterceptor = addCustomHeaderInterceptor;
        this.loggingRequestInterceptor = loggingRequestInterceptor;
    }

    @Bean
    RestTemplate restTemplate(@Autowired RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(applicationProperties.getHttpConnectionTimeout())
                .setReadTimeout(applicationProperties.getReadTimeout())
                .rootUri(applicationProperties.getVirtualcoinUrl())
                .additionalInterceptors(
                        Stream.of(addCustomHeaderInterceptor, loggingRequestInterceptor).filter(Objects::nonNull).collect(Collectors.toList())
                )
                .build();
    }

    @Bean(destroyMethod = "logout")
    @Autowired
    IDiscordClient discordClient(MessageSourceWrapper messageSource) {
        return new ClientBuilder()
                .withToken(applicationProperties.getDiscordBotToken())
                .withMinimumDispatchThreads(applicationProperties.getDiscordMinDispatchThreadCount())
                .withMaximumDispatchThreads(applicationProperties.getDiscordMaxDispatchThreadCount())
                .setPresence(StatusType.ONLINE, ActivityType.PLAYING, messageSource.getMessage("discord.bot.description"))
                .setMaxMessageCacheCount(applicationProperties.getDiscordMessageCache())
                .setMaxReconnectAttempts(applicationProperties.getDiscordReconnectAttempt())
                .build();
    }

    @Component
    public static class AddCustomHeaderInterceptor implements ClientHttpRequestInterceptor {

        private final ApplicationProperties applicationProperties;

        @Autowired
        public AddCustomHeaderInterceptor(ApplicationProperties applicationProperties) {
            this.applicationProperties = applicationProperties;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
            final String userPassword = applicationProperties.getRpcUser() + ":" + applicationProperties.getRpcPassword();
            final String token = Base64Utils.encodeToString(userPassword.getBytes());
            httpRequest.getHeaders().add("Authorization", "Basic " + token);
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        }
    }

    @Profile({"dev"})
    @Component
    @Slf4j
    public static class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
            traceRequest(httpRequest, bytes);
            final ClientHttpResponse response = new BufferingClientHttpResponseWrapper(clientHttpRequestExecution.execute(httpRequest, bytes));
            traceResponse(response);
            return response;
        }

        private void traceRequest(HttpRequest request, byte[] body) {
            log.debug("===========================request begin=============================================");
            log.debug("URI         : {}", request.getURI());
            log.debug("Method      : {}", request.getMethod());
            log.debug("Headers     : {}", request.getHeaders());
            log.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
            log.debug("==========================request end================================================");
        }

        private void traceResponse(ClientHttpResponse response) throws IOException {
            log.debug("============================response begin===========================================");
            log.debug("Status code  : {}", response.getStatusCode());
            log.debug("Status text  : {}", response.getStatusText());
            log.debug("Headers      : {}", response.getHeaders());
            log.debug("Response body: {}", IOUtils.toString(response.getBody(), Charset.defaultCharset()));
            log.debug("=======================response end===================================================");
        }

        static final class BufferingClientHttpResponseWrapper implements ClientHttpResponse {

            private final ClientHttpResponse response;

            private byte[] body;


            BufferingClientHttpResponseWrapper(ClientHttpResponse response) {
                this.response = response;
            }


            public HttpStatus getStatusCode() throws IOException {
                return this.response.getStatusCode();
            }

            public int getRawStatusCode() throws IOException {
                return this.response.getRawStatusCode();
            }

            public String getStatusText() throws IOException {
                return this.response.getStatusText();
            }

            public HttpHeaders getHeaders() {
                return this.response.getHeaders();
            }

            public InputStream getBody() throws IOException {
                if (this.body == null) {
                    this.body = StreamUtils.copyToByteArray(this.response.getBody());
                }
                return new ByteArrayInputStream(this.body);
            }

            public void close() {
                this.response.close();
            }

        }
    }
}

