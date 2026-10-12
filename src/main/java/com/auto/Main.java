package com.auto;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Main.class);
        Path configuration = findLocalConfiguration();
        if (configuration != null) {
            application.setDefaultProperties(Map.of(
                "spring.config.additional-location", configuration.toUri().toString()));
        }
        application.run(args);
    }

    private static Path findLocalConfiguration() {
        Path configuration = findAbove(Path.of("").toAbsolutePath());
        if (configuration != null) return configuration;
        try {
            // VS Code can start Java with a working directory outside the project.
            Path classes = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return findAbove(classes);
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Cannot locate backend configuration", exception);
        }
    }

    private static Path findAbove(Path directory) {
        for (int depth = 0; directory != null && depth < 6; depth++, directory = directory.getParent()) {
            Path local = directory.resolve("stripe-local.properties");
            if (Files.isRegularFile(local)) return local;
            Path backendLocal = directory.resolve("diagAuto-backend/stripe-local.properties");
            if (Files.isRegularFile(backendLocal)) return backendLocal;
        }
        return null;
    }
}
