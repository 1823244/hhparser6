package com.ens.hhparser5.controller;

import com.ens.hhparser5.configuration.AppConfig;
import com.ens.hhparser5.model.Employer;
import com.ens.hhparser5.service.EmployerService;
import com.ens.hhparser5.ui.Navbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Calendar;
import java.util.List;

/**
 * Spring MVC web-controller for Thymeleaf templates
 */
@Controller
@RequestMapping("employers")
public class EmployersController {

    private final Logger logger = LoggerFactory.getLogger(EmployersController.class);

    @Autowired
    private Navbar navbar;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private EmployerService employerService;

    @GetMapping()
    public ModelAndView listE(ModelMap model){
        model.addAttribute("navbarItems", navbar.getItems());


        model.addAttribute("today", Calendar.getInstance().getTime());

        model.addAttribute("pageTitle", "Employer's list");

        List<Employer> list = employerService.findAll();
        model.addAttribute("itemsList", list);

        return new ModelAndView("employers", model);
    }

}
