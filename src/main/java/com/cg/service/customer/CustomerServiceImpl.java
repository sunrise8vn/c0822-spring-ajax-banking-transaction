package com.cg.service.customer;

import com.cg.model.Customer;
import com.cg.model.Deposit;
import com.cg.model.Transfer;
import com.cg.repository.CustomerRepository;
import com.cg.repository.DepositRepository;
import com.cg.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class CustomerServiceImpl implements ICustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public List<Customer> findAllByDeletedIsFalse() {
        return customerRepository.findAllByDeletedIsFalse();
    }

    @Override
    public List<Customer> findAllByIdNot(Long id) {
        return customerRepository.findAllByIdNot(id);
    }

    @Override
    public void deposit(Customer customer, Deposit deposit) {
        Long customerId = customer.getId();
        BigDecimal transactionAmount = deposit.getTransactionAmount();

        deposit.setCustomer(customer);
        depositRepository.save(deposit);

        customerRepository.incrementBalance(customerId, transactionAmount);
    }

    @Override
    public void transfer(Transfer transfer) {
        Long senderId = transfer.getSender().getId();
        BigDecimal transactionAmount = transfer.getTransactionAmount();

        customerRepository.deIncrementBalance(senderId, transactionAmount);

        Long recipientId = transfer.getRecipient().getId();
        BigDecimal transferAmount = transfer.getTransferAmount();

        customerRepository.incrementBalance(recipientId, transferAmount);

        transferRepository.save(transfer);

    }

    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public void delete(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    public void delete(Customer customer) {
        customerRepository.delete(customer);
    }
}
