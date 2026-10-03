package dev.mayanktiwari.bank.security;

import dev.mayanktiwari.bank.domain.Account;
import dev.mayanktiwari.bank.repository.AccountRepository;
import dev.mayanktiwari.bank.support.CardNumbers;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accounts;

    public AccountUserDetailsService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String cardNumber = CardNumbers.normalize(username);
        Account account = accounts.findByCardNumber(cardNumber)
                .orElseThrow(() -> new UsernameNotFoundException("No account for this card number"));
        return new AccountPrincipal(account.getId(), account.getCardNumber(),
                account.getPinHash(), account.getLockedUntil());
    }
}
