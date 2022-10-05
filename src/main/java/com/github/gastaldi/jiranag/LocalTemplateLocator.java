package com.github.gastaldi.jiranag;

import io.quarkus.qute.TemplateLocator;
import io.quarkus.qute.Variant;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@ApplicationScoped
public class LocalTemplateLocator implements TemplateLocator {

    @Override
    public Optional<TemplateLocation> locate(String s) {
        final Path path = Path.of("./emails/" + s + ".html");
        if (path.toFile().exists()) {
            return Optional.of(new TemplateLocation() {
                @Override
                public Reader read() {
                    try {
                        return Files.newBufferedReader(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }

                @Override
                public Optional<Variant> getVariant() {
                    return Optional.empty();
                }
            });
        }
        return Optional.empty();
    }
}
