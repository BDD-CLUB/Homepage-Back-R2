package com.keeper.homepage.domain.merit.application;


import com.keeper.homepage.domain.member.application.convenience.MemberFindService;
import com.keeper.homepage.domain.member.entity.Member;
import com.keeper.homepage.domain.merit.dao.MeritLogRepository;
import com.keeper.homepage.domain.merit.dao.MeritTypeRepository;
import com.keeper.homepage.domain.merit.entity.MeritLog;
import com.keeper.homepage.domain.merit.entity.MeritType;
import com.keeper.homepage.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.keeper.homepage.global.error.ErrorCode.MERIT_TYPE_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeritLogService {

  private final MeritLogRepository meritLogRepository;
  private final MeritTypeRepository meritTypeRepository;
  private final MemberFindService memberFindService;

  @Transactional
  public Long recordMerit(long awarderId, long giverId, long meritTypeId) {
    Member awarder = memberFindService.findById(awarderId);
    Member giver = memberFindService.findById(giverId);
    MeritType meritType = meritTypeRepository.findById(meritTypeId)
        .orElseThrow(() -> new BusinessException(meritTypeId, "meritType", MERIT_TYPE_NOT_FOUND));

    updateMemberMeritDemerit(awarder, meritType);
    return meritLogRepository.save(MeritLog.builder()
            .awarder(awarder)
            .giver(giver)
            .meritType(meritType)
            .build())
        .getId();
  }

  public Page<MeritLog> findAll(Pageable pageable) {
    return meritLogRepository.findAll(pageable);
  }

  public Page<MeritLog> findAllByGiverId(Pageable pageable, long memberId) {
    long findMemberId = memberFindService.findById(memberId).getId();
    return meritLogRepository.findAllByGiverId(pageable, findMemberId);
  }

  private void updateMemberMeritDemerit(Member member, MeritType meritType) {
    if (meritType.getIsMerit()) {
      member.updateMerit(meritType.getMerit());
    } else {
      member.updateDemerit(meritType.getMerit());
    }
  }
}
