package com.marian.owncloudbackend.DTO;

import java.math.BigInteger;

public record UserDTO(
        Long id, String email,
        String role,
        BigInteger assignedSpace,
        BigInteger occupiedSpace) {
}
