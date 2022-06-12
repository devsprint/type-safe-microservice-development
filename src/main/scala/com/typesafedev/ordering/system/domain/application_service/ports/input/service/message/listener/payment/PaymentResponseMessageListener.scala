package com.typesafedev.ordering.system.domain.application_service.ports.input.service.message.listener.payment

import com.typesafedev.ordering.system.domain.application_service.dto.message.Messages.PaymentResponse

trait PaymentResponseMessageListener {

  def paymentCompleted(paymentResponse: PaymentResponse): Unit
  def paymentCancelled(paymentResponse: PaymentResponse): Unit

}
