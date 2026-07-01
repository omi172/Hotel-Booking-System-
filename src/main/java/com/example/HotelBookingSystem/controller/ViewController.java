package com.example.HotelBookingSystem.controller;

import com.example.HotelBookingSystem.DTO.RegisterRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class ViewController {

    @GetMapping("")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("User", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("user") RegisterRequest registerRequest,
                                      BindingResult result) {

        // 1. Check for form validation errors (if you use @Valid)
        if (result.hasErrors()) {
            return "register"; // Goes back to the form page to display error messages
        }

        // 2. Add your logic here (e.g., check passwords match, hash password, save to database)
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            // You can add custom errors to the binding result if passwords don't match
            result.rejectValue("password", "error.user", "Passwords do not match");
            return "register";
        }

        // 3. Redirect to a success or login page after successful registration
        return "redirect:/login?success";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/hotels")
    public String hotels() {
        return "hotels";
    }

    @GetMapping("/hotels/add")
    public String addHotel() {
        return "add-hotel";
    }

    @GetMapping("/search")
    public String search() {
        return "search";
    }

    @GetMapping("/bookings")
    public String bookings() {
        return "bookings";
    }

    @GetMapping("/bookings/new")
    public String newBooking() {
        return "new-booking";
    }

    @GetMapping("/payments")
    public String payments() {
        return "payments";
    }

    @GetMapping("/payments/new")
    public String newPayment() {
        return "new-payment";
    }
}


