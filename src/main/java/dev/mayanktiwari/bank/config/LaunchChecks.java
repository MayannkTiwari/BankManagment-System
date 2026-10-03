package dev.mayanktiwari.bank.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * In the "prod" profile the application will not start unless it is configured for a real,
 * custom domain served over HTTPS. This is the "we do not launch without our own domain" rule,
 * enforced in code instead of remembered.
 */
@Component
@Profile("prod")
public class LaunchChecks {

    private static final List<String> SHARED_HOST_SUFFIXES = List.of(
            ".onrender.com", ".herokuapp.com", ".vercel.app", ".netlify.app", ".railway.app",
            ".fly.dev", ".azurewebsites.net", ".github.io", ".pages.dev", ".web.app",
            ".firebaseapp.com", ".ngrok.io", ".ngrok-free.app", ".trycloudflare.com",
            ".replit.app", ".repl.co", ".glitch.me", ".cloudfront.net", ".amazonaws.com",
            ".elasticbeanstalk.com", ".run.app", ".appspot.com");

    private static final Pattern IPV4 = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

    public LaunchChecks(AppProperties properties) {
        List<String> problems = problems(properties);
        if (!problems.isEmpty()) {
            throw new IllegalStateException(
                    "Not ready to launch: " + String.join("; ", problems));
        }
    }

    static List<String> problems(AppProperties properties) {
        List<String> problems = new ArrayList<>();

        String host = null;
        try {
            URI uri = URI.create(properties.publicUrl().trim());
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                problems.add("APP_PUBLIC_URL must start with https://");
            }
            host = uri.getHost();
        } catch (IllegalArgumentException e) {
            problems.add("APP_PUBLIC_URL is not a valid URL");
        }

        if (host != null) {
            String h = host.toLowerCase(Locale.ROOT);
            if (h.equals("localhost") || !h.contains(".") || IPV4.matcher(h).matches()) {
                problems.add("APP_PUBLIC_URL must use your custom domain, not " + host);
            } else {
                for (String suffix : SHARED_HOST_SUFFIXES) {
                    if (h.endsWith(suffix)) {
                        problems.add("APP_PUBLIC_URL uses a shared hosting domain (" + host
                                + "). Add your custom domain first");
                        break;
                    }
                }
            }
        } else if (problems.isEmpty()) {
            problems.add("APP_PUBLIC_URL has no host name");
        }

        String email = properties.contactEmail().trim();
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            problems.add("APP_CONTACT_EMAIL must be a real email address");
        }
        return problems;
    }
}
