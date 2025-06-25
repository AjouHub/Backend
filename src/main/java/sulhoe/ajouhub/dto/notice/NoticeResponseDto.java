package sulhoe.ajouhub.dto.notice;

public record NoticeResponseDto(
        String number,
        String category,
        String title,
        String department,
        String date,
        String link
) {
}