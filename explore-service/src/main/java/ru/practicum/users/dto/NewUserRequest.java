package ru.practicum.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequest {

    @Size(min = 2, max = 250)
    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @Size(min = 6, max = 254)
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    private String email;
}
