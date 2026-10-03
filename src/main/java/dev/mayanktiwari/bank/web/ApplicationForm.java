package dev.mayanktiwari.bank.web;

import dev.mayanktiwari.bank.domain.AccountType;
import dev.mayanktiwari.bank.domain.Gender;
import dev.mayanktiwari.bank.domain.IncomeRange;
import dev.mayanktiwari.bank.domain.MaritalStatus;
import dev.mayanktiwari.bank.domain.Occupation;
import dev.mayanktiwari.bank.service.ApplicationService.NewApplication;
import dev.mayanktiwari.bank.validation.MinimumAge;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * The account application. Patterns accept an empty string so that a blank field produces only the
 * "required" message instead of two messages.
 */
public class ApplicationForm {

    private static final String NAME = "^(?:[\\p{L}\\p{M}][\\p{L}\\p{M} .'-]*)?$";

    private String token;

    @NotBlank(message = "Enter your full name")
    @Size(max = 100, message = "Use 100 characters or fewer")
    @Pattern(regexp = NAME, message = "Use letters, spaces, dots, hyphens and apostrophes only")
    private String fullName;

    @NotBlank(message = "Enter your father's name")
    @Size(max = 100, message = "Use 100 characters or fewer")
    @Pattern(regexp = NAME, message = "Use letters, spaces, dots, hyphens and apostrophes only")
    private String fatherName;

    @NotNull(message = "Enter your date of birth")
    @MinimumAge(18)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotNull(message = "Select a gender")
    private Gender gender;

    @NotBlank(message = "Enter your email address")
    @Size(max = 254, message = "Use 254 characters or fewer")
    @Pattern(regexp = "^(?:[^@\\s]+@[^@\\s]+\\.[^@\\s]+)?$", message = "Enter a valid email address")
    private String email;

    @NotNull(message = "Select your marital status")
    private MaritalStatus maritalStatus;

    @NotBlank(message = "Enter your address")
    @Size(max = 300, message = "Use 300 characters or fewer")
    private String address;

    @NotBlank(message = "Enter your city")
    @Size(max = 80, message = "Use 80 characters or fewer")
    private String city;

    @NotBlank(message = "Enter your PIN code")
    @Pattern(regexp = "^(?:[1-9][0-9]{5})?$", message = "Enter a 6-digit PIN code")
    private String pinCode;

    @NotNull(message = "Select your occupation")
    private Occupation occupation;

    @NotNull(message = "Select your annual income range")
    private IncomeRange incomeRange;

    @NotBlank(message = "Enter your PAN")
    @Pattern(regexp = "^(?:[A-Z]{5}[0-9]{4}[A-Z])?$", message = "Enter a valid PAN, for example ABCDE1234F")
    private String pan;

    @NotBlank(message = "Enter your Aadhaar number")
    @Pattern(regexp = "^(?:[2-9][0-9]{11})?$", message = "Enter a 12-digit Aadhaar number")
    private String aadhaar;

    @NotNull(message = "Select an account type")
    private AccountType accountType;

    @AssertTrue(message = "Confirm that the details above are correct")
    private boolean declaration;

    public NewApplication toNewApplication() {
        return new NewApplication(fullName, fatherName, dateOfBirth, gender, email, maritalStatus,
                address, city, pinCode, occupation, incomeRange, pan, aadhaar, accountType);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = trim(fullName);
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = trim(fatherName);
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = trim(email);
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = trim(address);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = trim(city);
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = stripWhitespace(pinCode);
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public void setOccupation(Occupation occupation) {
        this.occupation = occupation;
    }

    public IncomeRange getIncomeRange() {
        return incomeRange;
    }

    public void setIncomeRange(IncomeRange incomeRange) {
        this.incomeRange = incomeRange;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan == null ? null : stripWhitespace(pan).toUpperCase(Locale.ROOT);
    }

    public String getAadhaar() {
        return aadhaar;
    }

    public void setAadhaar(String aadhaar) {
        this.aadhaar = stripWhitespace(aadhaar);
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public boolean isDeclaration() {
        return declaration;
    }

    public void setDeclaration(boolean declaration) {
        this.declaration = declaration;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String stripWhitespace(String value) {
        return value == null ? null : value.replaceAll("\\s+", "");
    }
}
