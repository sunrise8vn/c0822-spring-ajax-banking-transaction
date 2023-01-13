package com.cg.controller;

import com.cg.model.Customer;
import com.cg.model.Deposit;
import com.cg.model.Transfer;
import com.cg.model.dto.DepositCreateDTO;
import com.cg.service.customer.ICustomerService;
import com.cg.service.deposit.IDepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private IDepositService depositService;

    @GetMapping
    public String showListPage(Model model) {

        List<Customer> customers = customerService.findAllByDeletedIsFalse();

        model.addAttribute("customers", customers);

        return "customer/list";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        model.addAttribute("customer", new Customer());

        return "customer/create";
    }

    @GetMapping("/edit/{id}")
    public String showEditPage(Model model, @PathVariable Long id) {

        Optional<Customer> customerOptional = customerService.findById(id);

        if (!customerOptional.isPresent()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Customer ID invalid");
            model.addAttribute("customer", new Customer());
        }
        else {
            Customer customer = customerOptional.get();

            if (customer.getDeleted()) {
                model.addAttribute("error", true);
                model.addAttribute("message", "This Customer is suspended");
            }
            else {
                model.addAttribute("error", null);
            }

            model.addAttribute("customer", customer);
        }

        return "customer/edit";
    }

    @GetMapping("/deposit/{customerId}")
    public String showDepositPage(@PathVariable Long customerId, Model model) {

        Optional<Customer> customerOptional = customerService.findById(customerId);

        if (!customerOptional.isPresent()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Customer ID invalid");
        }
        else {
            Customer customer = customerOptional.get();
            model.addAttribute("error", null);
            model.addAttribute("customer", customer);
//            model.addAttribute("deposit", new Deposit());
            model.addAttribute("depositCreateDTO", new DepositCreateDTO());
        }

        return "customer/deposit";
    }

    @GetMapping("/transfer/{senderId}")
    public String showTransferPage(@PathVariable Long senderId, Model model) {

        Optional<Customer> senderOptional = customerService.findById(senderId);

        if (!senderOptional.isPresent()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Sender ID invalid");
        }
        else {
            List<Customer> recipients = customerService.findAllByIdNot(senderId);

            Customer sender = senderOptional.get();
            model.addAttribute("error", null);
            model.addAttribute("sender", sender);
            model.addAttribute("recipients", recipients);
            model.addAttribute("transfer", new Transfer());
        }

        return "customer/transfer";
    }

    @GetMapping("/delete/{id}")
    public String showDeletePage(Model model, @PathVariable Long id) {

        Optional<Customer> customerOptional = customerService.findById(id);

        if (!customerOptional.isPresent()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Customer ID invalid");
            model.addAttribute("customer", new Customer());
        }
        else {
            Customer customer = customerOptional.get();

            if (customer.getDeleted()) {
                model.addAttribute("error", true);
                model.addAttribute("message", "This Customer is suspended");
            }
            else {
                model.addAttribute("error", null);
            }

            model.addAttribute("customer", customer);
        }

        return "customer/delete";
    }

    @PostMapping("/create")
    public String create(@Validated Customer customer, BindingResult bindingResult, Model model) {

        new Customer().validate(customer, bindingResult);

        if (bindingResult.hasFieldErrors()) {
            model.addAttribute("error", true);
            return "customer/create";
        }

        model.addAttribute("error", false);

        customer.setBalance(BigDecimal.ZERO);
        customerService.save(customer);

        return "redirect:/customers";
    }

    @PostMapping("/edit/{customerId}")
    public String update(Customer customer, @PathVariable Long customerId) {

        customer.setId(customerId);

        customerService.save(customer);

        return "redirect:/customers";
    }

    @PostMapping("/deposit/{customerId}")
    public String deposit(@PathVariable Long customerId, DepositCreateDTO depositCreateDTO, BindingResult bindingResult, Model model) {

        Optional<Customer> customerOptional = customerService.findById(customerId);

        if (!customerOptional.isPresent()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Customer ID invalid");
        }
        else {
            new DepositCreateDTO().validate(depositCreateDTO, bindingResult);

            if (bindingResult.hasFieldErrors()) {
                model.addAttribute("error", true);
                model.addAttribute("message", null);
                model.addAttribute("customer", customerOptional.get());
                return "customer/deposit";
            }

            Customer customer = customerOptional.get();
            BigDecimal currentBalance = customer.getBalance();
            BigDecimal transactionAmount = BigDecimal.valueOf(Long.parseLong(depositCreateDTO.getTransactionAmount()));
            BigDecimal newBalance = currentBalance.add(transactionAmount);
            customer.setBalance(newBalance);

            Deposit deposit = new Deposit();
            deposit.setId(null);
            deposit.setCustomer(customer);
            deposit.setTransactionAmount(transactionAmount);

            customerService.deposit(customer, deposit);

            model.addAttribute("error", false);
            model.addAttribute("message", null);
            model.addAttribute("customer", customer);
            model.addAttribute("deposit", new Deposit());
        }

        return "customer/deposit";
    }

    @PostMapping("/transfer/{senderId}")
    public String transfer(@PathVariable Long senderId, Transfer transfer, Model model) {

        Optional<Customer> senderOptional = customerService.findById(senderId);
        Customer sender = null;

        if (!senderOptional.isPresent()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Sender ID invalid");
        }
        else {
            sender = senderOptional.get();
            Long recipientId = transfer.getRecipient().getId();

            if (recipientId.equals(sender.getId())) {
                model.addAttribute("error", true);
                model.addAttribute("message", "Sender not same Recipient");
            }
            else {
                Optional<Customer> recipientOptional = customerService.findById(recipientId);

                if (!recipientOptional.isPresent()) {
                    model.addAttribute("error", true);
                    model.addAttribute("message", "Recipient ID invalid");
                }
                else {
                    BigDecimal currentSenderBalance = sender.getBalance();
                    BigDecimal transferAmount = transfer.getTransferAmount();
                    long fees = 10L;
                    BigDecimal feesAmount = transferAmount.multiply(BigDecimal.valueOf(fees)).divide(BigDecimal.valueOf(100L));
                    BigDecimal transactionAmount = transferAmount.add(feesAmount);

                    if (currentSenderBalance.compareTo(transactionAmount) < 0) {
                        model.addAttribute("error", true);
                        model.addAttribute("message", "Sender balance not enough to transaction");
                    }
                    else {
                        Customer recipient = recipientOptional.get();

                        transfer.setId(null);
                        transfer.setSender(sender);
                        transfer.setRecipient(recipient);
                        transfer.setFees(fees);
                        transfer.setFeesAmount(feesAmount);
                        transfer.setTransactionAmount(transactionAmount);

                        customerService.transfer(transfer);

                        model.addAttribute("error", false);
                        sender.setBalance(sender.getBalance().subtract(transactionAmount));
                    }
                }
            }
        }

        List<Customer> recipients = customerService.findAllByIdNot(senderId);

        model.addAttribute("sender", sender);
        model.addAttribute("recipients", recipients);
        model.addAttribute("transfer", new Transfer());

        return "customer/transfer";
    }

    @PostMapping("/delete/{customerId}")
    public String softDelete(@PathVariable Long customerId, Model model) {

        Optional<Customer> customerOptional = customerService.findById(customerId);

        if (!customerOptional.isPresent()) {
            model.addAttribute("error", true);
            model.addAttribute("message", "Customer ID invalid");
            model.addAttribute("customer", new Customer());
        }
        else {
            Customer customer = customerOptional.get();
            customer.setDeleted(true);
            customerService.save(customer);

            model.addAttribute("error", false);
            model.addAttribute("customer", customer);
        }

        return "customer/delete";
    }

}
