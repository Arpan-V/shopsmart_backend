package com.arpan.backend.dto.cart;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private Long cartId;

    private List<CartItemResponse> items;

    private BigDecimal totalPrice;

    private Integer totalItems;
}