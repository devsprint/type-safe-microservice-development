package com.typesafedev.ordering.system.domain.application_service.ports.input.service.message.listener.restaurantapproval

import com.typesafedev.ordering.system.domain.application_service.dto.message.Messages.RestaurantApprovalResponse

trait RestaurantApprovalMessageListener {

  def orderApproved(restaurantApprovalResponse: RestaurantApprovalResponse): Unit
  def orderRejected(restaurantApprovalResponse: RestaurantApprovalResponse): Unit

}
