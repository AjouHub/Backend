package sulhoe.ajouhub.dto.notice;

public record NoticeDto(String id,
                        String number,
                        String category,
                        String title,
                        String department,
                        String date,
                        String link) {
}
