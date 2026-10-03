package dev.mayanktiwari.bank.web;

import dev.mayanktiwari.bank.domain.AccountType;
import dev.mayanktiwari.bank.domain.Gender;
import dev.mayanktiwari.bank.domain.IncomeRange;
import dev.mayanktiwari.bank.domain.MaritalStatus;
import dev.mayanktiwari.bank.domain.Occupation;
import dev.mayanktiwari.bank.service.ApplicationService;
import dev.mayanktiwari.bank.service.ApplicationService.OpenedAccount;
import dev.mayanktiwari.bank.support.CardNumbers;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

@Controller
public class ApplyController {

    private static final String TOKEN_ATTRIBUTE = "applicationToken";

    private final ApplicationService applications;

    public ApplyController(ApplicationService applications) {
        this.applications = applications;
    }

    @ModelAttribute("genders")
    public Gender[] genders() {
        return Gender.values();
    }

    @ModelAttribute("maritalStatuses")
    public MaritalStatus[] maritalStatuses() {
        return MaritalStatus.values();
    }

    @ModelAttribute("occupations")
    public Occupation[] occupations() {
        return Occupation.values();
    }

    @ModelAttribute("incomeRanges")
    public IncomeRange[] incomeRanges() {
        return IncomeRange.values();
    }

    @ModelAttribute("accountTypes")
    public AccountType[] accountTypes() {
        return AccountType.values();
    }

    @GetMapping("/apply")
    public String form(Model model, HttpSession session) {
        ApplicationForm form = new ApplicationForm();
        String token = UUID.randomUUID().toString();
        session.setAttribute(TOKEN_ATTRIBUTE, token);
        form.setToken(token);
        model.addAttribute("form", form);
        return "apply";
    }

    @PostMapping("/apply")
    public String submit(@Valid @ModelAttribute("form") ApplicationForm form,
                         BindingResult result,
                         HttpSession session,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "apply";
        }

        // The token makes a double click or a refresh create one account, not two.
        synchronized (WebUtils.getSessionMutex(session)) {
            Object expected = session.getAttribute(TOKEN_ATTRIBUTE);
            if (expected == null || !expected.equals(form.getToken())) {
                result.reject("application.expired",
                        "This form was already submitted or has expired. Reload the page to start again.");
                return "apply";
            }
            session.removeAttribute(TOKEN_ATTRIBUTE);
        }

        OpenedAccount opened = applications.open(form.toNewApplication());

        redirect.addFlashAttribute("openedCardNumber", CardNumbers.grouped(opened.cardNumber()));
        redirect.addFlashAttribute("openedPin", opened.pin());
        redirect.addFlashAttribute("openedType", opened.accountType().label());
        return "redirect:/apply/done";
    }

    @GetMapping("/apply/done")
    public String done(Model model) {
        if (!model.containsAttribute("openedCardNumber")) {
            return "redirect:/login";
        }
        return "apply-done";
    }
}
