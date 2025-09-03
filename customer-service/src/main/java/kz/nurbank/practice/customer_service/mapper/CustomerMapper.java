package kz.nurbank.practice.customer_service.mapper;

import kz.nurbank.practice.customer_service.dto.CustomerDTO;
import kz.nurbank.practice.customer_service.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerMapper {

    public static Customer toEntity(CustomerDTO dto) {
        return Customer.builder()
                .fullName(dto.getFullName())
                .iin(dto.getIin())
                .phone(dto.getPhone())
                .build();
    }

    public static CustomerDTO toDTO(Customer entity) {
        return CustomerDTO.builder()
                .fullName(entity.getFullName())
                .iin(entity.getIin())
                .phone(entity.getPhone())
                .build();
    }
}