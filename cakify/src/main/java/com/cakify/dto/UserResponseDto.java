package com.cakify.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private boolean enabled;
}
