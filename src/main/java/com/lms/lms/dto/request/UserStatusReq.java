package com.lms.lms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusReq {

    // both optional; only the provided flags are applied
    private Boolean isActive;

    private Boolean isBanned;
}
