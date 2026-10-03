package dev.mayanktiwari.bank.security;

import dev.mayanktiwari.bank.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginAttemptService attempts;

    public LoginSuccessHandler(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (authentication.getPrincipal() instanceof AccountPrincipal principal) {
            attempts.reset(principal.getAccountId());
        }
        response.sendRedirect(request.getContextPath() + "/account");
    }
}
