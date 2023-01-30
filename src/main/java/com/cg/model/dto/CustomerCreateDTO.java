package com.cg.model.dto;

import com.cg.model.Customer;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.Valid;
import java.math.BigDecimal;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
public class CustomerCreateDTO implements Validator {

    private Long id;
    private String fullName;
    private String email;
    private String phone;

    @Valid
    private LocationRegionDTO locationRegion;

    public Customer toCustomer(BigDecimal balance) {
        return new Customer()
                .setId(id)
                .setFullName(fullName)
                .setEmail(email)
                .setPhone(phone)
                .setBalance(balance)
                .setLocationRegion(locationRegion.toLocationRegion())
                ;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return CustomerCreateDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CustomerCreateDTO customerCreateDTO = (CustomerCreateDTO) target;

        String fullName = customerCreateDTO.getFullName();
        String email = customerCreateDTO.getEmail();

        if (fullName.length() == 0) {
            errors.rejectValue("fullName", "fullName.null", "Full Name is required");
        }

        if (fullName.length() > 20) {
            errors.rejectValue("fullName", "fullName.max", "Full Name max length is 20 characters");
        }

        if (email.length() == 0) {
            errors.rejectValue("email", "email.null", "Email is required");
        }


        if (!email.matches("(^[\\w]+@([\\w-]+\\.)+[\\w-]{2,6}$)")) {
            errors.rejectValue("email", "email.matches", "Email invalid");
        }

    }

}
