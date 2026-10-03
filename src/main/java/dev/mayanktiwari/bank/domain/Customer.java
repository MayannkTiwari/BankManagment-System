package dev.mayanktiwari.bank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "father_name", nullable = false, length = 100)
    private String fatherName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", nullable = false, length = 20)
    private MaritalStatus maritalStatus;

    @Column(name = "address", nullable = false, length = 300)
    private String address;

    @Column(name = "city", nullable = false, length = 80)
    private String city;

    @Column(name = "pin_code", nullable = false, length = 6)
    private String pinCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "occupation", nullable = false, length = 30)
    private Occupation occupation;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_range", nullable = false, length = 30)
    private IncomeRange incomeRange;

    /** The PAN, encrypted with AES-256-GCM. Never stored in plain text. */
    @Column(name = "pan_encrypted", nullable = false, length = 200)
    private String panEncrypted;

    /** Only the last four digits of the Aadhaar number are kept. */
    @Column(name = "aadhaar_last4", nullable = false, length = 4)
    private String aadhaarLast4;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Customer() {
    }

    public Customer(String fullName, String fatherName, LocalDate dateOfBirth, Gender gender,
                    String email, MaritalStatus maritalStatus, String address, String city,
                    String pinCode, Occupation occupation, IncomeRange incomeRange,
                    String panEncrypted, String aadhaarLast4, Instant createdAt) {
        this.fullName = fullName;
        this.fatherName = fatherName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.email = email;
        this.maritalStatus = maritalStatus;
        this.address = address;
        this.city = city;
        this.pinCode = pinCode;
        this.occupation = occupation;
        this.incomeRange = incomeRange;
        this.panEncrypted = panEncrypted;
        this.aadhaarLast4 = aadhaarLast4;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPinCode() {
        return pinCode;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public IncomeRange getIncomeRange() {
        return incomeRange;
    }

    public String getPanEncrypted() {
        return panEncrypted;
    }

    public String getAadhaarLast4() {
        return aadhaarLast4;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
