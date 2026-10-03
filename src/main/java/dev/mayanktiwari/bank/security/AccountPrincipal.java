package dev.mayanktiwari.bank.security;

import java.io.Serial;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** The signed-in account. The username is the card number and the password is the PIN hash. */
public class AccountPrincipal implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long accountId;
    private final String cardNumber;
    private final String pinHash;
    private final Instant lockedUntil;

    public AccountPrincipal(Long accountId, String cardNumber, String pinHash, Instant lockedUntil) {
        this.accountId = accountId;
        this.cardNumber = cardNumber;
        this.pinHash = pinHash;
        this.lockedUntil = lockedUntil;
    }

    public Long getAccountId() {
        return accountId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Override
    public String getPassword() {
        return pinHash;
    }

    @Override
    public String getUsername() {
        return cardNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || !lockedUntil.isAfter(Instant.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
