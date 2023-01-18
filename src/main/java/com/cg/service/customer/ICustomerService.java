package com.cg.service.customer;

import com.cg.model.Customer;
import com.cg.model.Deposit;
import com.cg.model.Transfer;
import com.cg.model.dto.CustomerDTO;
import com.cg.service.IGeneralService;

import java.util.List;


public interface ICustomerService extends IGeneralService<Customer> {

    List<Customer> findAllByDeletedIsFalse();

    List<CustomerDTO> findAllCustomerDTOByDeletedIsFalse();

    List<Customer> findAllByIdNot(Long id);

    void deposit(Customer customer, Deposit deposit);

    void transfer(Transfer transfer);
}
