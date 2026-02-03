package com.trk.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    public String token;
    public String type = "Bearer";
    public Long id;
    public String email;
    public String username;
    public String referralCode;
}
