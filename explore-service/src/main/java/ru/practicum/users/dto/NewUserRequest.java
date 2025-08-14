package ru.practicum.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequest {
    @Size(min = 2, max = 250)
    @NotBlank(message = "Field: name. Error: must not be blank. Value: null")
    private String name;

    @Size(min = 6, max = 254)
    @NotBlank(message = "Field: email. Error: must not be blank. Value: null")
    @Email(message = "invalid format email")
    private String email;
}
