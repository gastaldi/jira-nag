package com.github.gastaldi.jiranag;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

@ConfigMapping(prefix = "app")
public interface JiraNagConfig {

    Jira jira();

    Email email();

    interface Jira {
        @WithDefault("https://issues.redhat.com")
        URI url();

        String token();

        String query();
    }

    interface Email {
        String subject();

        Optional<String> to();

        String replyTo();
    }
}

