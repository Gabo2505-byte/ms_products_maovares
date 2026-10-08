package com.maovares.ms_products;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderQueueService {

    private final ObjectMapper mapper;
    private final String connectionString;
    private final String queueName;
    private volatile QueueClient client;

    public OrderQueueService(ObjectMapper mapper,
                             @Value("${AZURE_STORAGE_CONNECTION_STRING:}") String connectionString,
                             @Value("${ORDERS_QUEUE_NAME:ordersqueue}") String queueName) {
        this.mapper = mapper;
        this.connectionString = connectionString;
        this.queueName = queueName;
    }

    public void send(OrderRequest order) throws Exception {
        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException("AZURE_STORAGE_CONNECTION_STRING no configurada");
        }
        OrderRequest event = order.createdAt() == null
                ? new OrderRequest(order.orderId(), order.customerEmail(), order.customerName(),
                        order.total(), order.items(), Instant.now().toString())
                : order;
        String json = mapper.writeValueAsString(event);
        // La Function (trigger de cola) espera el mensaje en base64
        String b64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        queue().sendMessage(b64);
    }

    private QueueClient queue() {
        if (client == null) {
            client = new QueueClientBuilder()
                    .connectionString(connectionString)
                    .queueName(queueName)
                    .buildClient();
        }
        return client;
    }
}