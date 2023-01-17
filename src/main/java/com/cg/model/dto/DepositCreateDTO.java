package com.cg.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DepositCreateDTO implements Validator {

    private Long id;
    private String transactionAmount;


    @Override
    public boolean supports(Class<?> clazz) {
        return DepositCreateDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        DepositCreateDTO depositCreateDTO = (DepositCreateDTO) target;

        String transactionAmountStr = depositCreateDTO.getTransactionAmount();

        if (transactionAmountStr.length() == 0) {
            errors.rejectValue("transactionAmount", "transactionAmount.null");
        }
        else {
            if (!transactionAmountStr.matches("(^$|[0-9]*$)")){
                errors.rejectValue("transactionAmount", "transactionAmount.matches");
            }
        }
    }
}
