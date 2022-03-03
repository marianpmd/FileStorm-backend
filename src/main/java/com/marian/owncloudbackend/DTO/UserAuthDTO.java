package com.marian.owncloudbackend.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

public record UserAuthDTO (String email,String password){}
