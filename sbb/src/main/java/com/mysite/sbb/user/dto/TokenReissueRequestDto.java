package com.mysite.sbb.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenReissueRequestDto {
    @NotBlank(message = "refreshToken은 필수항목입니다.")
    private String refreshToken;

}
