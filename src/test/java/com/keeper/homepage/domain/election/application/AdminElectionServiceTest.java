package com.keeper.homepage.domain.election.application;

import static com.keeper.homepage.domain.member.entity.job.MemberJob.MemberJobType.ROLE_회장;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

import com.keeper.homepage.IntegrationTest;
import com.keeper.homepage.domain.election.entity.Election;
import com.keeper.homepage.domain.election.entity.ElectionCandidate;
import com.keeper.homepage.domain.member.entity.Member;
import com.keeper.homepage.domain.member.entity.job.MemberJob;
import com.keeper.homepage.domain.member.entity.job.MemberJob.MemberJobType;
import com.keeper.homepage.global.error.BusinessException;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


public class AdminElectionServiceTest extends IntegrationTest {

  @Nested
  @DisplayName("선거 삭제 테스트")
  class DeleteElection {

    @Test
    @DisplayName("선거 삭제 시 삭제가 성공한다.")
    public void 선거_삭제_시_삭제가_성공한다() throws Exception {
      Election election = electionTestHelper.builder()
          .name("제 1회 임원진 선거")
          .description("임원진 선거입니다.")
          .isAvailable(false)
          .build();

      long electionId = election.getId();
      adminElectionService.deleteElection(electionId);

      em.flush();
      em.clear();

      assertThat(electionRepository.findById(electionId)).isEmpty();
    }

    @Test
    @DisplayName("선거가 공개 상태라면 삭제는 실패한다.")
    public void 선거가_공개_상태라면_삭제는_실패한다() throws Exception {
      Election election = electionTestHelper.builder()
          .name("제 1회 임원진 선거")
          .description("임원진 선거입니다.")
          .isAvailable(true)
          .build();

      long electionId = election.getId();

      assertThrows(BusinessException.class, () -> adminElectionService.deleteElection(electionId));
    }

  }

  @Nested
  @DisplayName("후보자 등록 및 삭제 테스트")
  class CandidateRegisterAndDeleteTest {

    private MemberJob memberJob;
    private ElectionCandidate electionCandidate;
    private long memberJobId = 1;

    @BeforeEach
    void setUp() {
      memberJob = memberJobRepository.findById(memberJobId).orElseThrow();
      electionCandidate = electionCandidateTestHelper.generate(memberJob);
    }

    @Test
    @DisplayName("후보자 등록에 성공한다.")
    public void 후보자_등록에_성공한다() throws Exception {
      long electionId = electionCandidate.getElection().getId();
      long candidateId = electionCandidate.getMember().getId();
      adminElectionService.registerCandidate(electionCandidate.getDescription(), memberJob.getId(),
          electionId, candidateId);

      em.flush();
      em.clear();

      ElectionCandidate savedCandidate = electionCandidateRepository.findById(electionCandidate.getId()).orElseThrow();

      assertEquals(electionCandidate.getDescription(), savedCandidate.getDescription());
      assertThat(savedCandidate.getMemberJob().getType()).isEqualTo(ROLE_회장);

    }

    @Test
    @DisplayName("후보자에 적절하지 않는 memberJobId 라면 등록 실패한다.")
    public void 후보자에_적절하지_않는_memberJobId_라면_등록_실패한다() throws Exception {
      MemberJobType improperMemberJobType = MemberJobType.ROLE_대외부장;
      long electionId = electionCandidate.getElection().getId();
      long candidateId = electionCandidate.getMember().getId();
      memberJob = memberJobRepository.findById(improperMemberJobType.getId()).orElseThrow();

      BusinessException exception = assertThrows(BusinessException.class,
          () -> adminElectionService.registerCandidate(electionCandidate.getDescription(), memberJob.getId(),
              electionId, candidateId));
      assertEquals("해당 직위는 후보자 등록 불가합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("후보자 다중 등록에 성공한다.")
    public void 후보자_다중_등록에_성공한다() throws Exception {
      Election election = electionTestHelper.generate();
      long electionId = election.getId();

      List<Member> members = IntStream.range(0, 5)
          .mapToObj(member -> memberTestHelper.generate())
          .toList();

      List<Long> candidateIds = members.stream()
          .map(Member::getId)
          .toList();

      electionCandidateRepository.deleteAll();
      adminElectionService.registerCandidates(candidateIds, electionCandidate.getDescription(), memberJob.getId(),
          electionId);

      em.flush();
      em.clear();

      List<ElectionCandidate> savedCandidate = electionCandidateRepository.findAll();

      assertEquals(5, savedCandidate.size());
    }

    @Test
    @DisplayName("후보자 삭제에 성공한다.")
    public void 후보자_삭제에_성공한다() throws Exception {
      long electionId = electionCandidate.getElection().getId();
      long candidateId = electionCandidate.getId();
      adminElectionService.deleteCandidate(electionId, candidateId);

      em.flush();
      em.clear();

      assertThat(electionCandidateRepository.findById(candidateId)).isEmpty();
    }

    @Test
    @DisplayName("후보자 정보가 없다면 후보자 삭제에 실패한다.")
    public void 후보자_정보가_없다면_후보자_삭제에_실패한다() throws Exception {
      long electionId = electionCandidate.getElection().getId();
      long notExistCandidateId = 10;

      BusinessException exception = assertThrows(BusinessException.class,
          () -> adminElectionService.deleteCandidate(electionId, notExistCandidateId));
      assertEquals("해당 후보자를 찾을 수 없습니다.", exception.getMessage());
    }

  }

}

