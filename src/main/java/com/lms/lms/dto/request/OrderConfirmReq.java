package com.lms.lms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmReq {

    // UPI transaction id, bank reference, receipt number... required so a manually
    // confirmed order stays traceable back to money that actually arrived
    @NotBlank(message = "Payment Reference Is Required")
    private String paymentReference;
}
