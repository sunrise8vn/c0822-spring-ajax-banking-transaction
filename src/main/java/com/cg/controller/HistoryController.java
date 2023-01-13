package com.cg.controller;


import com.cg.model.Transfer;
import com.cg.service.transfer.ITransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/histories")
public class HistoryController {

    @Autowired
    private ITransferService transferService;


    @GetMapping("/transfer")
    public String showTransferHistoryPage(Model model) {

        List<Transfer> transfers = transferService.findAll();
        model.addAttribute("transfers", transfers);

        return "histories/transfer";
    }
}
