package sulhoe.ajouhub.dto.user;

import sulhoe.ajouhub.dto.notice.NoticeDto;

import java.util.List;

public record UserWithSavedNoticesDto(Long id,
                                      String email,
                                      String department,
                                      List<NoticeDto> savedNotices) {
}
