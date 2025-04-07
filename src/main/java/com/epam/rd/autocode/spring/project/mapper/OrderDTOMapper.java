package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderDTOMapper {
    private ModelMapper mapper;

    public OrderDTOMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public OrderDTO convertOrderToOrderDTO(Order order) {
        log.debug("Mapping Order to OrderDTO for order id: {}", order.getId());
        return mapper.map(order, OrderDTO.class);
    }

    public Order convertOrderDTOToOrder(OrderDTO orderDTO) {
        log.debug("Mapping OrderDTO to Order for client email: {}", orderDTO.getClientEmail());
        return mapper.map(orderDTO, Order.class);
    }

}
