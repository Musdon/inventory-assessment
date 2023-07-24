package com.musdon.orderservice.service;

import com.musdon.orderservice.dto.OrderRequest;
import com.musdon.orderservice.model.Order;
import com.musdon.orderservice.model.OrderItems;
import com.musdon.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class OrderService {

    private ModelMapper modelMapper;
    private OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderItems> orderItemsList =orderRequest.getOrderItemsList().stream()
                .map(orderItemsDto -> modelMapper.map(orderItemsDto, OrderItems.class))
                .toList();
        order.setOrderItemsList(orderItemsList);

        orderRepository.save(order);
    }
}
