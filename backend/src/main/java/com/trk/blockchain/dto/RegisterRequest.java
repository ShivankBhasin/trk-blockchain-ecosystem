package com.trk.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    @Email
    public String email;

    @NotBlank
    @Size(min = 6)
    public String password;

    @NotBlank
    @Size(min = 3, max = 20)
    public String username;

    public String referralCode;
}
