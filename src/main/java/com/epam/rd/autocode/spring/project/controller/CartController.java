package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.Cart;
import com.epam.rd.autocode.spring.project.service.CartService;
import com.epam.rd.autocode.spring.project.service.impl.CartServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes("cart")
@RequestMapping("/cart")
@Slf4j
public class CartController {

    private final CartService cartService;

    public CartController(CartServiceImpl cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("cart")
    public Cart cart() {
        log.info("Creating new Cart instance");
        return new Cart();
    }

    @GetMapping
    public String showCart(@ModelAttribute("cart") Cart cart, Model model) {
        log.info("Displaying cart contents");
        model.addAttribute("items", cart.getItems());
        return "cart";
    }

    @GetMapping("/add")
    public String addBookToCart(@RequestParam("name") String bookName,
                                @ModelAttribute("cart") Cart cart,
                                RedirectAttributes redirectAttributes) {
        log.info("Adding book {} to cart", bookName);
        try {
            cartService.addBookToCart(bookName, cart);
        } catch (Exception e) {
            log.error("Error adding book {} to cart: {}", bookName, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "cart.add.error");
        }
        return "redirect:/cart";
    }

    @GetMapping("/remove")
    public String removeBookFromCart(@RequestParam("name") String bookName,
                                     @ModelAttribute("cart") Cart cart,
                                     RedirectAttributes redirectAttributes) {
        log.info("Removing book {} from cart", bookName);
        try {
            cartService.removeBookFromCart(bookName, cart);
        } catch (Exception e) {
            log.error("Error removing book {} from cart: {}", bookName, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "cart.remove.error");
        }
        return "redirect:/cart";
    }

    @GetMapping("/clear")
    public String clearCart(@ModelAttribute("cart") Cart cart,
                            RedirectAttributes redirectAttributes) {
        log.info("Clearing the cart");
        try {
            cart.clearCart();
        } catch (Exception e) {
            log.error("Error clearing cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "cart.clear.error");
        }
        return "redirect:/cart";
    }
}

