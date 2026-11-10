package com.outfitai.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Database entity. Never returned from controllers directly: use UserResponse (a DTO). */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider;

    @Column(name = "google_sub", unique = true)
    private String googleSub;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected User() {
        // required by JPA
    }

    public static User local(String email, String passwordHash, String displayName) {
        User u = new User();
        u.email = email;
        u.passwordHash = passwordHash;
        u.displayName = displayName;
        u.authProvider = AuthProvider.LOCAL;
        return u;
    }

    public static User google(String email, String googleSub, String displayName) {
        User u = new User();
        u.email = email;
        u.googleSub = googleSub;
        u.displayName = displayName;
        u.authProvider = AuthProvider.GOOGLE;
        return u;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public AuthProvider getAuthProvider() { return authProvider; }
    public String getGoogleSub() { return googleSub; }
    public String getDisplayName() { return displayName; }
    public Integer getAge() { return age; }
    public Gender getGender() { return gender; }
    public Instant getCreatedAt() { return createdAt; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setAge(Integer age) { this.age = age; }
    public void setGender(Gender gender) { this.gender = gender; }
}
