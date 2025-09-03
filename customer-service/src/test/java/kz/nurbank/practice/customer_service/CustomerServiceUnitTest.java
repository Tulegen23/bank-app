//package kz.nurbank.practice.customer_service;
//
//import kz.nurbank.practice.customer_service.dto.CustomerDTO;
//import kz.nurbank.practice.customer_service.mapper.CustomerMapper;
//import kz.nurbank.practice.customer_service.model.Customer;
//import kz.nurbank.practice.customer_service.repository.CustomerRepository;
//import kz.nurbank.practice.customer_service.service.CustomerServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//public class CustomerServiceUnitTest {
//
//    private CustomerRepository repository;
//    private CustomerServiceImpl service;
//
//    @BeforeEach
//    void setup() {
//        repository = Mockito.mock(CustomerRepository.class);
//    }
//
//    @Test
//    void testSaveCustomer() {
//        CustomerDTO dto = CustomerDTO.builder()
//                .fullName("Test User")
//                .iin("123456789012")
//                .phone("+77071234567")
//                .build();
//
//        Customer expected = CustomerMapper.toEntity(dto);
//        expected.setId(1L);
//
//        when(repository.save(any(Customer.class))).thenReturn(expected);
//
//        Customer result = service.save(dto);
//
//        assertThat(result.getId()).isEqualTo(1L);
//        verify(repository, times(1)).save(any(Customer.class));
//    }
//
//    @Test
//    void testFindByIin() {
//        Customer customer = Customer.builder()
//                .id(2L)
//                .fullName("Another User")
//                .iin("987654321098")
//                .phone("+77077654321")
//                .build();
//
//        when(repository.findByIin("987654321098")).thenReturn(Optional.of(customer));
//
//        Optional<Customer> found = service.findByIin("987654321098");
//
//        assertThat(found).isPresent();
//        assertThat(found.get().getId()).isEqualTo(2L);
//    }
//}
