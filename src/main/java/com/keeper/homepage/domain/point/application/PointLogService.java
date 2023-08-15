package com.keeper.homepage.domain.point.application;

import static java.time.LocalDateTime.*;

import com.keeper.homepage.domain.attendance.entity.Attendance;
import com.keeper.homepage.domain.member.application.convenience.MemberFindService;
import com.keeper.homepage.domain.member.entity.Member;
import com.keeper.homepage.domain.point.dao.PointLogRepository;
import com.keeper.homepage.domain.point.entity.PointLog;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointLogService {

  private final PointLogRepository pointLogRepository;
  private final MemberFindService memberFindService;

  private static final String ATTENDANCE_POINT_MESSAGE = "출석 포인트";

  @Transactional
  public void create(Attendance attendance) {
    pointLogRepository.save(PointLog.builder()
        .time(attendance.getTime())
        .member(attendance.getMember())
        .point(attendance.getTotalPoint())
        .detail(ATTENDANCE_POINT_MESSAGE)
        .isSpent(true)
        .build());
  }

  @Transactional
  public Long create(Member giver, Member receiver, int point, String message) {
    boolean isSpent = point < 0;
    return pointLogRepository.save(PointLog.builder()
        .time(now())
        .member(giver)
        .point(point)
        .detail(message)
        .presented(receiver)
        .isSpent(isSpent)
        .build()).getId();
  }

  public Page<PointLog> findAllPointLogs(Pageable pageable, long memberId) {
    return pointLogRepository.findAllByMemberId(pageable, memberId);
  }
}
