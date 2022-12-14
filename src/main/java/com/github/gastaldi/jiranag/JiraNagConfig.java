package com.github.gastaldi.jiranag;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@ConfigMapping(prefix = "app")
public interface JiraNagConfig {

    String profile();

    Map<String, String> profiles();

    Jira jira();

    Email email();

    /**
     * @return the current profile
     */
    default String currentJiraQuery() {
        return profiles().get(profile());
    }

    interface Jira {
        @WithDefault("https://issues.redhat.com")
        URI url();

        String token();
    }

    interface Email {
        String subject();

        Optional<String> to();

        String replyTo();
    }
}

