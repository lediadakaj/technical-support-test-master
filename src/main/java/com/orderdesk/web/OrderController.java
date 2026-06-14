package com.orderdesk.web;

import com.orderdesk.dto.NoteDto;
import com.orderdesk.dto.NoteRequest;
import com.orderdesk.dto.OrderDetailDto;
import com.orderdesk.dto.OrderSummaryDto;
import com.orderdesk.model.OrderNote;
import com.orderdesk.repository.OrderNoteRepository;
import com.orderdesk.repository.OrderRepository;
import com.orderdesk.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final DateTimeFormatter NOTE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final OrderRepository orderRepository;
    private final OrderNoteRepository orderNoteRepository;
    private final OrderService orderService;

    public OrderController(OrderRepository orderRepository,
                           OrderNoteRepository orderNoteRepository,
                           OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderNoteRepository = orderNoteRepository;
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderSummaryDto> list(@RequestParam(name = "orderNumber", required = false) String orderNumber) {
        var orders = (orderNumber == null || orderNumber.isBlank())
                ? orderRepository.findAll()
                : orderRepository.findByOrderNumberContainingIgnoreCaseOrderByOrderNumber(orderNumber.trim());
        return orders.stream().map(orderService::toSummary).toList();
    }

    @GetMapping("/{id}")
    public OrderDetailDto get(@PathVariable Long id) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return orderService.toDetail(order);
    }

    @GetMapping("/{id}/notes")
    public List<NoteDto> notes(@PathVariable Long id) {
        return orderNoteRepository.findByOrderIdOrderByCreatedAtAsc(id).stream()
                .map(this::toNoteDto)
                .toList();
    }

    @PostMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteDto addNote(@PathVariable Long id, @Valid @RequestBody NoteRequest request) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        var note = new OrderNote(order, "Support agent", request.body().trim(), LocalDateTime.now());
        return toNoteDto(orderNoteRepository.save(note));
    }

    private NoteDto toNoteDto(OrderNote note) {
        return new NoteDto(
                note.getId(),
                note.getAuthor(),
                note.getBody(),
                note.getCreatedAt().format(NOTE_DATE_FORMAT));
    }
}
