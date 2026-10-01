package com.hvtc.bakery.controller;

import com.hvtc.bakery.entity.Contact;
import com.hvtc.bakery.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final ContactRepository contactRepository;

    @PostMapping("/contact/submit")
    public String submitContact(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "subject", required = false) String subject,
            @RequestParam("message") String message,
            RedirectAttributes redirectAttributes
    ) {
        Contact contact = Contact.builder()
                .name(name.trim())
                .email(email.trim())
                .phone(phone != null ? phone.trim() : null)
                .subject(subject != null ? subject.trim() : "Liên hệ từ website")
                .message(message.trim())
                .build();

        contactRepository.save(contact);
        redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi tin nhắn! HVTC BAKERY sẽ phản hồi trong thời gian sớm nhất.");
        return "redirect:/contact";
    }
}
