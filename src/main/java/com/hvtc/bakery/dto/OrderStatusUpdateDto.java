package com.hvtc.bakery.dto;

import com.hvtc.bakery.entity.OrderStatus;
import com.hvtc.bakery.entity.PaymentStatus;
import lombok.Data;

@Data
public class OrderStatusUpdateDto {
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
}
