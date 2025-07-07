package sulhoe.ajouhub.dto.notice;

import sulhoe.ajouhub.entity.Notice;

import java.util.List;

public record NoticeDto(String id,
                        String number,
                        String category,
                        String title,
                        String department,
                        String date,
                        String link) {
    public static NoticeDto fromEntity(Notice notice) {
        return new NoticeDto(
                notice.getId().toString(),
                notice.getNumber(),
                notice.getCategory(),
                notice.getTitle(),
                notice.getDepartment(),
                notice.getDate(),
                notice.getLink()
        );
    }

    public static List<NoticeDto> toDtoList(List<Notice> notices) {
        return notices.stream()
                .map(NoticeDto::fromEntity)
                .toList();
    }

}

