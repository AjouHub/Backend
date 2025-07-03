package sulhoe.ajouhub.dto.user;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        String department
) {
}
