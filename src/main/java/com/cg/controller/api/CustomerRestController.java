package com.cg.controller.api;

import com.cg.exception.DataInputException;
import com.cg.model.Customer;
import com.cg.model.Deposit;
import com.cg.model.LocationRegion;
import com.cg.model.Transfer;
import com.cg.model.dto.CustomerCreateDTO;
import com.cg.model.dto.CustomerDTO;
import com.cg.model.dto.TransferReqDTO;
import com.cg.service.customer.ICustomerService;
import com.cg.utils.AppUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/customers")
public class CustomerRestController {

    @Autowired
    private AppUtils appUtils;

    @Autowired
    private ICustomerService customerService;

    @GetMapping
    public ResponseEntity<?> getALlCustomers() {

//        List<Customer> customers = customerService.findAllByDeletedIsFalse();
        List<CustomerDTO> customers = customerService.findAllCustomerDTOByDeletedIsFalse();

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

        customerService.save(customer);

        return new ResponseEntity<>(customer, HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<?> doCreate(@Validated @RequestBody CustomerCreateDTO customerCreateDTO, BindingResult bindingResult) {

        new CustomerCreateDTO().validate(customerCreateDTO, bindingResult);

        if (bindingResult.hasFieldErrors()) {
            return appUtils.mapErrorToResponse(bindingResult);
        }

//        LocationRegion locationRegion = customerCreateDTO.getLocationRegion().toLocationRegion();
        Customer customer = customerCreateDTO.toCustomer(BigDecimal.ZERO);

        customerService.save(customer);

        CustomerDTO customerDTO = customer.toCustomerDTO();

        return new ResponseEntity<>(customerDTO, HttpStatus.CREATED);
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
    public ResponseEntity<?> doTransfer(@Validated @RequestBody TransferReqDTO transferReqDTO, BindingResult bindingResult) {

        new TransferReqDTO().validate(transferReqDTO, bindingResult);

        if (bindingResult.hasFieldErrors()) {
            return appUtils.mapErrorToResponse(bindingResult);
        }

        String senderIdStr = transferReqDTO.getSenderId();
        String recipientIdStr = transferReqDTO.getRecipientId();

        Long senderId = Long.parseLong(senderIdStr);
        Long recipientId = Long.parseLong(recipientIdStr);


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
        recipient = recipientOptional.get();

        if (senderId.equals(recipientId)) {
            throw new DataInputException("Sender not same Recipient");
        }

        BigDecimal currentSenderBalance = sender.getBalance();
        BigDecimal transferAmount = BigDecimal.valueOf(Long.parseLong(transferReqDTO.getTransferAmount()));
        long fees = 10L;
        BigDecimal feesAmount = transferAmount.multiply(BigDecimal.valueOf(fees)).divide(BigDecimal.valueOf(100L));
        BigDecimal transactionAmount = transferAmount.add(feesAmount);

        if (currentSenderBalance.compareTo(transactionAmount) < 0) {
            throw new DataInputException("Sender balance not enough to transfer transaction");
        }


        Transfer transfer = new Transfer();
        transfer.setSender(sender);
        transfer.setRecipient(recipient);
        transfer.setTransferAmount(transferAmount);
        transfer.setFees(fees);
        transfer.setFeesAmount(feesAmount);
        transfer.setTransactionAmount(transactionAmount);

        customerService.transfer(transfer);

        sender.setBalance(sender.getBalance().subtract(transactionAmount));
        recipient.setBalance(recipient.getBalance().add(transferAmount));


        CustomerDTO senderDTO = sender.toCustomerDTO();

        CustomerDTO recipientDTO = recipient.toCustomerDTO();

        Map<String, CustomerDTO> result = new HashMap<>();
        result.put("sender", senderDTO);
        result.put("recipient", recipientDTO);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
