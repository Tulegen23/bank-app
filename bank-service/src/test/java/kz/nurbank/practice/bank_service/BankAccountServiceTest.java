//package kz.nurbank.practice.bank_service;
//
//import kz.nurbank.practice.bank_service.model.BankAccount;
//import kz.nurbank.practice.bank_service.repository.BankAccountRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//public class BankAccountServiceTest {
//
//    @Autowired
//    private BankAccountRepository repository;
//
//    @Test
//    void testSaveAccount() {
//        BankAccount account = new BankAccount();
//        account.setClientId(1L);
//        account.setAccountNumber("K2-1234567890");
//
//        BankAccount saved = repository.save(account);
//
//        assertThat(saved.getId()).isNotNull();
//        assertThat(saved.getClientId()).isEqualTo(1L);
//        assertThat(saved.getAccountNumber()).isEqualTo("K2-1234567890");
//    }
//}
