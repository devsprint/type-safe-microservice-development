package com.typesafedev.ordering.system.domain.application_service.ports.output.repository.message.publisher.restaurantapproval

import com.typesafedev.ordering.system.domain.core.events.Events.DomainEvent.OrderPaidEvent
import com.typesafedev.ordering.system.domain.core.events.publisher.Publisher.DomainEventPublisher

trait OrderPaidRestaurantRequestMessagePublisher extends DomainEventPublisher[OrderPaidEvent]{

}
