package br.com.study;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    @DisplayName("Should Create Money Successfully")
    void shouldCreateMoneySuccessfully() {
        Money money = new Money(new BigDecimal(100));
        assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal(100));
    }

    @Test
    @DisplayName("Should Throw Exception When Amount Is Null")
    void shouldThrowExceptionWhenAmountIsNull() {
        assertThatThrownBy(() -> new Money(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount cannot be null");

    }

    @Test
    @DisplayName("Should not allow negative amount")
    void shouldThrowExceptionWhenAmountIsNegative() {

        assertThatThrownBy(() -> new Money(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Money cannot be negative");
    }

    @Test
    @DisplayName("Should add two Money values correctly")
    void shouldAddMoneyCorrectly() {

        Money m1 = new Money(new BigDecimal("100"));
        Money m2 = new Money(new BigDecimal("50"));

        Money result = m1.add(m2);

        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("150"));
        // immutability check
        assertThat(m1.getAmount()).isEqualByComparingTo("100");
        assertThat(m2.getAmount()).isEqualByComparingTo("50");
    }

    @Test
    @DisplayName("Should subtract two Money values correctly")
    void shouldSubtractMoneyCorrectly() {

        Money m1 = new Money(new BigDecimal("100"));
        Money m2 = new Money(new BigDecimal("30"));

        Money result = m1.subtract(m2);

        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("70"));
        // immutability check
        assertThat(m1.getAmount()).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("Should throw exception when subtraction result is negative")
    void shouldThrowWhenSubtractionBecomesNegative() {

        Money m1 = new Money(new BigDecimal("10"));
        Money m2 = new Money(new BigDecimal("20"));

        assertThatThrownBy(() -> m1.subtract(m2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Money cannot be negative");
    }

    @Test
    @DisplayName("Money should be immutable")
    void moneyShouldBeImmutable() {

        Money m1 = new Money(new BigDecimal("100"));
        Money m2 = m1.add(new Money(new BigDecimal("50")));

        assertThat(m1.getAmount()).isEqualByComparingTo("100");
        assertThat(m2.getAmount()).isEqualByComparingTo("150");
    }

}