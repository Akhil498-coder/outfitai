package com.outfitai.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

/** Uses the real configured datasource (H2 profile) so Flyway's V2 migration is exercised too. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    void savesAndFindsByEmail() {
        User saved = repository.saveAndFlush(User.local("a@example.com", "hash", "Alex"));

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findByEmail("a@example.com")).get()
                .extracting(User::getAuthProvider, User::getDisplayName)
                .containsExactly(AuthProvider.LOCAL, "Alex");
        assertThat(repository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void emailMustBeUnique() {
        repository.saveAndFlush(User.google("dup@example.com", "sub-1", "G"));

        assertThatThrownBy(() -> repository.saveAndFlush(User.local("dup@example.com", "hash", "L")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void storesOptionalProfileFields() {
        User u = User.local("p@example.com", "hash", "Pat");
        u.setAge(30);
        u.setGender(Gender.OTHER);
        repository.saveAndFlush(u);

        assertThat(repository.findByEmail("p@example.com")).get()
                .extracting(User::getAge, User::getGender)
                .containsExactly(30, Gender.OTHER);
    }
}
