package com.maovares.ms_products;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/Orders")
public class OrderController {

    private final OrderQueueService queueService;

    public OrderController(OrderQueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody OrderRequest order) {
        if (order == null
                || order.orderId() == null || order.orderId().isBlank()
                || order.customerEmail() == null || order.customerEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "orderId y customerEmail son obligatorios"));
        }
        try {
            queueService.send(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Cola no configurada"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "No se pudo encolar el pedido"));
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("message", "Order " + order.orderId() + " enqueued successfully"));
    }
}