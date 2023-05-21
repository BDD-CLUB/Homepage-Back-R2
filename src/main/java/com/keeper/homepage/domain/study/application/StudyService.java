package com.keeper.homepage.domain.study.application;

import static com.keeper.homepage.global.error.ErrorCode.POST_CANNOT_ACCESSIBLE;
import static com.keeper.homepage.global.error.ErrorCode.STUDY_CANNOT_ACCESSIBLE;

import com.keeper.homepage.domain.member.entity.Member;
import com.keeper.homepage.domain.study.application.convenience.StudyFindService;
import com.keeper.homepage.domain.study.dao.StudyRepository;
import com.keeper.homepage.domain.study.entity.Study;
import com.keeper.homepage.domain.thumbnail.entity.Thumbnail;
import com.keeper.homepage.global.error.BusinessException;
import com.keeper.homepage.global.util.thumbnail.ThumbnailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

  private final StudyRepository studyRepository;
  private final ThumbnailUtil thumbnailUtil;
  private final StudyFindService studyFindService;

  public void create(Study study, MultipartFile thumbnail) {
    saveStudyThumbnail(study, thumbnail);
    studyRepository.save(study);
  }

  private void saveStudyThumbnail(Study study, MultipartFile thumbnail) {
    Thumbnail savedThumbnail = thumbnailUtil.saveThumbnail(thumbnail).orElse(null);
    study.changeThumbnail(savedThumbnail);
  }

  public void delete(Member member, long studyId) {
    Study study = studyFindService.findById(studyId);

    if (!member.isHeadMember(study)) {
      throw new BusinessException(study.getId(), "study", STUDY_CANNOT_ACCESSIBLE);
    }
    studyRepository.delete(study);
  }
}
