//// CustomerServiceTest.java
//package kz.nurbank.practice.customer_service;
//
//import kz.nurbank.practice.customer_service.model.Customer;
//import kz.nurbank.practice.customer_service.repository.CustomerRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//public class CustomerServiceTest {
//
//    @Autowired
//    private CustomerRepository repository;
//
//    @Test
//    void testSaveCustomer() {
//        Customer customer = Customer.builder()
//                .fullName("Test User")
//                .iin("123456789012")
//                .phone("+77071234567")
//                .build();
//        Customer saved = repository.save(customer);
//        assertThat(saved.getId()).isNotNull();
//    }
//}