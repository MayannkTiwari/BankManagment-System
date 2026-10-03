package dev.mayanktiwari.bank.security;

import dev.mayanktiwari.bank.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Counts wrong PINs and always sends the same generic message, so the page never reveals
 * whether a card number exists or whether an account is locked.
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final LoginAttemptService attempts;

    public LoginFailureHandler(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        if (exception instanceof BadCredentialsException) {
            attempts.recordFailure(request.getParameter("cardNumber"));
        }
        response.sendRedirect(request.getContextPath() + "/login?error");
    }
}
