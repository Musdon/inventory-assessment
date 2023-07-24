package com.musdon.orderservice.service;

import com.musdon.orderservice.dto.InventoryResponse;
import com.musdon.orderservice.dto.OrderRequest;
import com.musdon.orderservice.model.Order;
import com.musdon.orderservice.model.OrderItems;
import com.musdon.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class OrderService {

    private ModelMapper modelMapper;
    private OrderRepository orderRepository;
    private WebClient webClient;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderItems> orderItemsList =orderRequest.getOrderItemsList().stream()
                .map(orderItemsDto -> modelMapper.map(orderItemsDto, OrderItems.class))
                .toList();
        order.setOrderItemsList(orderItemsList);
        List<String> skuCodes = order.getOrderItemsList().stream().map(OrderItems::getSkuCode).toList();
        //call order service if product is in stock
        InventoryResponse[] inventoryResponseArray = webClient.get().uri("http://localhost:8082:/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build()).retrieve()
                        .bodyToMono(InventoryResponse[].class)
                                .block();
        boolean allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::getIsInStock);
        if (allProductsInStock){
            orderRepository.save(order);
        }
        else {
            throw new IllegalArgumentException("Product is not in stock, please try again later.");
        }

    }
}
