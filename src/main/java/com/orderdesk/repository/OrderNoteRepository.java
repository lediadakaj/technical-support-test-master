package com.orderdesk.repository;

import com.orderdesk.model.OrderNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderNoteRepository extends JpaRepository<OrderNote, Long> {

    List<OrderNote> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
