package au.com.mason.expensemanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticateResponseDto {
    final String status;
    final String user;
}
