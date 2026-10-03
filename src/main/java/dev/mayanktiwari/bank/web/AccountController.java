package dev.mayanktiwari.bank.web;

import dev.mayanktiwari.bank.config.AppProperties;
import dev.mayanktiwari.bank.security.AccountPrincipal;
import dev.mayanktiwari.bank.service.AccountQueryService;
import dev.mayanktiwari.bank.service.BankingService;
import dev.mayanktiwari.bank.service.OperationResult;
import dev.mayanktiwari.bank.service.Outcome;
import dev.mayanktiwari.bank.service.PinService;
import dev.mayanktiwari.bank.support.CardNumbers;
import dev.mayanktiwari.bank.support.Formats;
import dev.mayanktiwari.bank.support.Pins;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final AccountQueryService queries;
    private final BankingService banking;
    private final PinService pins;
    private final AppProperties properties;

    public AccountController(AccountQueryService queries, BankingService banking,
                             PinService pins, AppProperties properties) {
        this.queries = queries;
        this.banking = banking;
        this.pins = pins;
        this.properties = properties;
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal AccountPrincipal principal, Model model) {
        model.addAttribute("account", queries.account(principal.getAccountId()));
        model.addAttribute("entries", queries.recentEntries(principal.getAccountId()));
        return "account/dashboard";
    }

    @GetMapping("/statement")
    public String statement(@AuthenticationPrincipal AccountPrincipal principal,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            Model model) {
        model.addAttribute("account", queries.account(principal.getAccountId()));
        model.addAttribute("statement", queries.statement(principal.getAccountId(), page));
        return "account/statement";
    }

    // ---- Deposit ----

    @GetMapping("/deposit")
    public String depositForm(Model model) {
        model.addAttribute("form", new AmountForm());
        return "account/deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@AuthenticationPrincipal AccountPrincipal principal,
                          @Valid @ModelAttribute("form") AmountForm form,
                          BindingResult result,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "account/deposit";
        }
        OperationResult outcome = banking.deposit(principal.getAccountId(), form.getAmount(), form.getRequestId());
        switch (outcome.outcome()) {
            case OK -> {
                redirect.addFlashAttribute("notice", "Deposited " + Formats.rupees(form.getAmount())
                        + ". Your balance is " + Formats.rupees(outcome.balance()) + ".");
                return "redirect:/account";
            }
            case DUPLICATE -> {
                return alreadyProcessed(redirect);
            }
            case OVER_LIMIT -> result.rejectValue("amount", "amount.limit", overLimitMessage());
            default -> throw new IllegalStateException("Unexpected outcome " + outcome.outcome());
        }
        return "account/deposit";
    }

    // ---- Withdrawal ----

    @GetMapping("/withdraw")
    public String withdrawForm(Model model) {
        model.addAttribute("form", new PinAmountForm());
        return "account/withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@AuthenticationPrincipal AccountPrincipal principal,
                           @Valid @ModelAttribute("form") PinAmountForm form,
                           BindingResult result,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "account/withdraw";
        }
        OperationResult outcome = banking.withdraw(
                principal.getAccountId(), form.getAmount(), form.getPin(), form.getRequestId());
        switch (outcome.outcome()) {
            case OK -> {
                redirect.addFlashAttribute("notice", "Withdrew " + Formats.rupees(form.getAmount())
                        + ". Your balance is " + Formats.rupees(outcome.balance()) + ".");
                return "redirect:/account";
            }
            case DUPLICATE -> {
                return alreadyProcessed(redirect);
            }
            default -> rejectFailure(result, outcome.outcome(), "pin", "amount");
        }
        return "account/withdraw";
    }

    // ---- Transfer ----

    @GetMapping("/transfer")
    public String transferForm(Model model) {
        model.addAttribute("form", new TransferForm());
        return "account/transfer";
    }

    @PostMapping("/transfer")
    public String transfer(@AuthenticationPrincipal AccountPrincipal principal,
                           @Valid @ModelAttribute("form") TransferForm form,
                           BindingResult result,
                           RedirectAttributes redirect) {
        String recipient = CardNumbers.normalize(form.getRecipientCard());
        if (!result.hasFieldErrors("recipientCard") && !CardNumbers.isValid(recipient)) {
            result.rejectValue("recipientCard", "recipient.invalid", "Enter a valid 16-digit card number");
        }
        if (result.hasErrors()) {
            return "account/transfer";
        }
        OperationResult outcome = banking.transfer(
                principal.getAccountId(), recipient, form.getAmount(), form.getPin(), form.getRequestId());
        switch (outcome.outcome()) {
            case OK -> {
                redirect.addFlashAttribute("notice", "Transferred " + Formats.rupees(form.getAmount())
                        + " to the account ending " + CardNumbers.lastFour(recipient)
                        + ". Your balance is " + Formats.rupees(outcome.balance()) + ".");
                return "redirect:/account";
            }
            case DUPLICATE -> {
                return alreadyProcessed(redirect);
            }
            case RECIPIENT_NOT_FOUND ->
                    result.rejectValue("recipientCard", "recipient.unknown", "No account was found with this card number");
            case SAME_ACCOUNT ->
                    result.rejectValue("recipientCard", "recipient.same", "You cannot transfer to your own account");
            default -> rejectFailure(result, outcome.outcome(), "pin", "amount");
        }
        return "account/transfer";
    }

    // ---- Change PIN ----

    @GetMapping("/pin")
    public String pinForm(Model model) {
        model.addAttribute("form", new ChangePinForm());
        return "account/pin";
    }

    @PostMapping("/pin")
    public String changePin(@AuthenticationPrincipal AccountPrincipal principal,
                            @Valid @ModelAttribute("form") ChangePinForm form,
                            BindingResult result,
                            RedirectAttributes redirect) {
        if (!result.hasFieldErrors("newPin") && !Pins.isAcceptable(form.getNewPin())) {
            result.rejectValue("newPin", "pin.weak",
                    "Choose a PIN that is not all the same digit and not a run such as 1234");
        }
        if (!result.hasFieldErrors("confirmPin") && !result.hasFieldErrors("newPin")
                && !form.getNewPin().equals(form.getConfirmPin())) {
            result.rejectValue("confirmPin", "pin.mismatch", "The two new PINs do not match");
        }
        if (result.hasErrors()) {
            return "account/pin";
        }
        Outcome outcome = pins.changePin(principal.getAccountId(), form.getCurrentPin(), form.getNewPin());
        switch (outcome) {
            case OK -> {
                redirect.addFlashAttribute("notice", "Your PIN has been changed.");
                return "redirect:/account";
            }
            case SAME_PIN ->
                    result.rejectValue("newPin", "pin.same", "Choose a PIN different from your current one");
            default -> rejectFailure(result, outcome, "currentPin", null);
        }
        return "account/pin";
    }

    // ---- helpers ----

    private void rejectFailure(BindingResult result, Outcome outcome, String pinField, String amountField) {
        switch (outcome) {
            case WRONG_PIN -> result.rejectValue(pinField, "pin.wrong", "Incorrect PIN");
            case LOCKED -> result.reject("account.locked",
                    "Too many incorrect PIN attempts. This account is locked for "
                            + properties.security().lockMinutes() + " minutes.");
            case INSUFFICIENT_FUNDS -> result.rejectValue(amountField, "amount.funds",
                    "Your balance is not enough for this amount");
            case OVER_LIMIT -> result.rejectValue(amountField, "amount.limit", overLimitMessage());
            default -> throw new IllegalStateException("Unexpected outcome " + outcome);
        }
    }

    private String overLimitMessage() {
        return "The limit is " + Formats.rupees(properties.limits().maxTransaction()) + " per transaction";
    }

    private static String alreadyProcessed(RedirectAttributes redirect) {
        redirect.addFlashAttribute("notice",
                "That request was already processed. Check your statement for the details.");
        return "redirect:/account";
    }
}
