package com.cg.model.dto;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


public class DepositCreateDTO implements Validator {

    private Long id;
    private String transactionAmount;

    public DepositCreateDTO() {
    }

    public DepositCreateDTO(Long id, String transactionAmount) {
        this.id = id;
        this.transactionAmount = transactionAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

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
