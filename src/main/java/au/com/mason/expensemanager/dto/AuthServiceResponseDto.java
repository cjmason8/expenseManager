package au.com.mason.expensemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AuthServiceResponseDto {
    private String tokenStatus;
    private String user;
    public boolean isValid() {
        return tokenStatus.equals("valid");
    }
}
