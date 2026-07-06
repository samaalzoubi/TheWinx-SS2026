package com.winx.payment.api.ui;

import com.winx.payment.api.dto.CreatePaymentRequest;
import com.winx.payment.domain.exception.PaymentNotFoundException;
import com.winx.payment.domain.model.Payment;
import com.winx.payment.domain.model.PaymentStatus;
import com.winx.payment.domain.repository.PaymentRepository;
import com.winx.payment.domain.service.PaymentProcessingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Server-rendered UI on top of the same PaymentProcessingService/PaymentRepository
 * used by the REST API (PaymentController). None of the /api/payments contracts are
 * touched here - this controller only adds human-friendly views, including a
 * standalone "Create Test Payment" page so bc04 can be exercised without going
 * through bc03-booking's flow first.
 */
@Controller
@RequestMapping("/ui")
public class PaymentUiController {

    private static final List<String> PAYMENT_METHODS = List.of("CARD", "PAYPAL");

    private final PaymentRepository paymentRepository;
    private final PaymentProcessingService paymentProcessingService;

    public PaymentUiController(PaymentRepository paymentRepository, PaymentProcessingService paymentProcessingService) {
        this.paymentRepository = paymentRepository;
        this.paymentProcessingService = paymentProcessingService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status, Model model) {
        List<Payment> all = paymentRepository.findAll();
        all.sort(Comparator.comparing(Payment::getId, Comparator.nullsLast(Comparator.reverseOrder())));

        long totalCount = all.size();
        long paidCount = all.stream().filter(p -> p.getResult().getStatus() == PaymentStatus.PAID).count();
        long failedCount = all.stream().filter(p -> p.getResult().getStatus() == PaymentStatus.FAILED).count();
        long pendingCount = all.stream().filter(p -> p.getResult().getStatus() == PaymentStatus.PENDING).count();
        BigDecimal totalRevenue = all.stream()
                .filter(p -> p.getResult().getStatus() == PaymentStatus.PAID)
                .map(p -> p.getMoney().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String activeFilter = (status == null || status.isBlank()) ? "ALL" : status.toUpperCase();
        List<Payment> filtered = all;
        if (!"ALL".equals(activeFilter)) {
            try {
                PaymentStatus filterStatus = PaymentStatus.valueOf(activeFilter);
                filtered = all.stream().filter(p -> p.getResult().getStatus() == filterStatus).toList();
            } catch (IllegalArgumentException ex) {
                activeFilter = "ALL";
            }
        }

        model.addAttribute("payments", filtered);
        model.addAttribute("activeFilter", activeFilter);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("failedCount", failedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("totalRevenue", totalRevenue);
        return "payments-list";
    }

    @GetMapping("/payments/new")
    public String newPaymentForm(Model model) {
        model.addAttribute("paymentMethods", PAYMENT_METHODS);
        return "payment-new";
    }

    @GetMapping("/payments/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> PaymentNotFoundException.forId(id));
        model.addAttribute("payment", payment);
        return "payment-detail";
    }

    @PostMapping("/payments")
    public String create(@RequestParam Long bookingId,
                          @RequestParam Long userId,
                          @RequestParam BigDecimal amount,
                          @RequestParam String currency,
                          @RequestParam String paymentMethod,
                          @RequestParam(required = false) String maskedReference,
                          RedirectAttributes redirectAttributes) {
        CreatePaymentRequest form = new CreatePaymentRequest(bookingId, userId, amount, currency, paymentMethod, maskedReference);
        Payment payment = paymentProcessingService.initiatePayment(
                form.bookingId(),
                form.amount(),
                form.currency(),
                form.userId(),
                form.paymentMethod(),
                form.maskedReferenceOrDefault());
        redirectAttributes.addFlashAttribute("message", "Created payment " + payment.getId()
                + " with status " + payment.getResult().getStatus());
        return "redirect:/ui";
    }

    /**
     * Standalone "Create Test Payment" flow. Goes through the exact same
     * PaymentProcessingService.initiatePayment(...) call as PaymentController's
     * POST /api/payments - same validation, same mock gateway, same persistence -
     * so a real PAID/FAILED payment is created without needing bc03-booking.
     */
    @PostMapping("/payments/new")
    public String createNew(@RequestParam Long bookingId,
                             @RequestParam Long userId,
                             @RequestParam BigDecimal amount,
                             @RequestParam(defaultValue = "EUR") String currency,
                             @RequestParam String paymentMethod,
                             @RequestParam(required = false) String maskedReference,
                             RedirectAttributes redirectAttributes) {
        CreatePaymentRequest form = new CreatePaymentRequest(bookingId, userId, amount, currency, paymentMethod, maskedReference);
        Payment payment = paymentProcessingService.initiatePayment(
                form.bookingId(),
                form.amount(),
                form.currency(),
                form.userId(),
                form.paymentMethod(),
                form.maskedReferenceOrDefault());
        redirectAttributes.addFlashAttribute("message",
                "Payment #" + payment.getId() + " created - result: " + payment.getResult().getStatus());
        return "redirect:/ui/payments/" + payment.getId();
    }

    @PostMapping("/payments/{id}/retry")
    public String retry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Payment payment = paymentProcessingService.retryPayment(id);
        redirectAttributes.addFlashAttribute("message",
                "Retried payment #" + payment.getId() + " - result: " + payment.getResult().getStatus());
        return "redirect:/ui/payments/" + id;
    }
}
