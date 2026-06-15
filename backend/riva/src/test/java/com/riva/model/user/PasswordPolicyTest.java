package com.riva.model.user;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.riva.exception.ValidationException;

class PasswordPolicyTest {

    @Test
    void aceptaPasswordConLongitudMinima() {
        assertThatCode(() -> PasswordPolicy.validar("12345678")).doesNotThrowAnyException();
    }

    @Test
    void rechazaPasswordCorta() {
        assertThatThrownBy(() -> PasswordPolicy.validar("1234567"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("8 caracteres");
    }

    @Test
    void rechazaPasswordNula() {
        assertThatThrownBy(() -> PasswordPolicy.validar(null))
                .isInstanceOf(ValidationException.class);
    }
}
