package com.cg.controller.api;

import com.cg.exception.DataInputException;
import com.cg.model.Customer;
import com.cg.model.Deposit;
import com.cg.model.Transfer;
import com.cg.model.dto.DepositCreateDTO;
import com.cg.service.customer.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {

    @Autowired
    private ICustomerService customerService;

    @GetMapping
    public ResponseEntity<List<Customer>> getALlCustomers() {

        List<Customer> customers = customerService.findAllByDeletedIsFalse();

        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long customerId) {

        Optional<Customer> customer = customerService.findById(customerId);

        return new ResponseEntity<>(customer.get(), HttpStatus.OK);
    }

    @GetMapping("/get-all-recipients-with-out-sender/{senderId}")
    public ResponseEntity<List<Customer>> getAllRecipientsWithOutSender(@PathVariable Long senderId) {

        Optional<Customer> customerOptional = customerService.findById(senderId);

        if (!customerOptional.isPresent()) {

        }

        List<Customer> recipients = customerService.findAllByIdNot(senderId);

        return new ResponseEntity<>(recipients, HttpStatus.OK);
    }

    @PatchMapping("/{customerId}")
    public ResponseEntity<Customer> doUpdate(@PathVariable Long customerId, @RequestBody Customer reqCustomer) {

        Optional<Customer> optionalCustomer = customerService.findById(customerId);

        if (!optionalCustomer.isPresent()) {

        }

        Customer customer = optionalCustomer.get();
        customer.setFullName(reqCustomer.getFullName());
        customer.setPhone(reqCustomer.getPhone());
        customer.setAddress(reqCustomer.getAddress());

        customerService.save(customer);

        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    @PostMapping("/deposit")
    public ResponseEntity<Customer> doDeposit(@RequestBody Deposit deposit) {

        Long customerId = deposit.getCustomer().getId();

        Optional<Customer> optionalCustomer = customerService.findById(customerId);

        if (!optionalCustomer.isPresent()) {

        }

        Customer customer = optionalCustomer.get();

        BigDecimal currentBalance = customer.getBalance();
        BigDecimal transactionAmount = deposit.getTransactionAmount();
        BigDecimal newBalance = currentBalance.add(transactionAmount);
        customer.setBalance(newBalance);

        customerService.deposit(customer, deposit);


        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> doTransfer(@RequestBody Transfer transfer) {

        Long senderId = transfer.getSender().getId();
        Long recipientId = transfer.getRecipient().getId();

        Optional<Customer> senderOptional = customerService.findById(senderId);
        Customer sender = null;

        if (!senderOptional.isPresent()) {
            throw new NullPointerException("Sender invalid");
        }

        Optional<Customer> recipientOptional = customerService.findById(recipientId);
        Customer recipient = null;

        if (!recipientOptional.isPresent()) {
            throw new DataInputException("Recipient invalid");
        }

        sender = senderOptional.get();

        BigDecimal currentSenderBalance = sender.getBalance();
        BigDecimal transferAmount = transfer.getTransferAmount();
        long fees = 10L;
        BigDecimal feesAmount = transferAmount.multiply(BigDecimal.valueOf(fees)).divide(BigDecimal.valueOf(100L));
        BigDecimal transactionAmount = transferAmount.add(feesAmount);

        if (currentSenderBalance.compareTo(transactionAmount) < 0) {
            throw new DataInputException("Sender balance not enough to transfer transaction");
        }

        recipient = recipientOptional.get();

        transfer.setId(null);
        transfer.setSender(sender);
        transfer.setRecipient(recipient);
        transfer.setFees(fees);
        transfer.setFeesAmount(feesAmount);
        transfer.setTransactionAmount(transactionAmount);

        customerService.transfer(transfer);

        sender.setBalance(sender.getBalance().subtract(transactionAmount));
        recipient.setBalance(recipient.getBalance().add(transferAmount));

        Map<String, Customer> result = new HashMap<>();
        result.put("sender", sender);
        result.put("recipient", recipient);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
