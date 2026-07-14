package com.lms.lms.dto.response;

import com.lms.lms.modals.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AccountStatusRes {
    private Boolean isVerified;
    private Boolean isActive;
    private Boolean isBanned;
    private Boolean isDeleted;

    public static AccountStatusRes from(User user) {
        return new AccountStatusRes(
                user.getIsVerified(),
                user.getIsActive(),
                user.getIsBanned(),
                user.getIsDeleted()
        );
    }
}
