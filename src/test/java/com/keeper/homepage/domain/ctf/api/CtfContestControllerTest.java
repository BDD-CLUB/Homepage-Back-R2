package com.keeper.homepage.domain.ctf.api;

import static com.keeper.homepage.domain.member.entity.job.MemberJob.MemberJobType.ROLE_회원;
import static com.keeper.homepage.domain.member.entity.job.MemberJob.MemberJobType.ROLE_회장;
import static com.keeper.homepage.global.config.security.data.JwtType.ACCESS_TOKEN;
import static com.keeper.homepage.global.restdocs.RestDocsHelper.getSecuredValue;
import static com.keeper.homepage.global.restdocs.RestDocsHelper.pageHelper;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.keeper.homepage.IntegrationTest;
import com.keeper.homepage.domain.ctf.dto.request.contest.CreateContestRequest;
import com.keeper.homepage.domain.ctf.dto.request.contest.UpdateContestRequest;
import com.keeper.homepage.domain.ctf.entity.CtfContest;
import com.keeper.homepage.domain.member.entity.Member;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;

public class CtfContestControllerTest extends IntegrationTest {

  private Member admin;
  private String adminToken;

  @BeforeEach
  void setUp() {
    admin = memberTestHelper.generate();
    adminToken = jwtTokenProvider.createAccessToken(ACCESS_TOKEN, admin.getId(), ROLE_회원, ROLE_회장);
  }

  @Nested
  @DisplayName("CTF 대회 생성 테스트")
  class CtfContestCreateTest {

    @Test
    @DisplayName("유효한 요청일 경우 CTF 대회 생성은 성공한다.")
    public void 유효한_요청일_경우_CTF_대회_생성은_성공한다() throws Exception {
      String securedValue = getSecuredValue(CtfContestController.class, "createContest");

      CreateContestRequest request = CreateContestRequest.builder()
          .name("2024 KEEPER CTF")
          .description("2024 KEEPER CTF 입니다.")
          .build();

      mockMvc.perform(post("/ctf/contests")
              .content(asJsonString(request))
              .cookie(new Cookie(ACCESS_TOKEN.getTokenName(), adminToken))
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isCreated())
          .andDo(document("create-ctf-contest",
              requestCookies(
                  cookieWithName(ACCESS_TOKEN.getTokenName())
                      .description("ACCESS TOKEN %s".formatted(securedValue))
              ),
              requestFields(
                  fieldWithPath("name").description("CTF 대회 명"),
                  fieldWithPath("description").description("CTF 대회 설명")
              )));
    }
  }

  @Nested
  @DisplayName("CTF 대회 수정 테스트")
  class CtfContestUpdateTest {

    @Test
    @DisplayName("유효한 요청일 경우 CTF 대회 수정은 성공한다.")
    public void 유효한_요청일_경우_CTF_대회_수정은_성공한다() throws Exception {
      String securedValue = getSecuredValue(CtfContestController.class, "updateContest");

      CtfContest ctfContest = ctfContestTestHelper.generate();

      UpdateContestRequest request = UpdateContestRequest.builder()
          .name("2024 KEEPER CTF")
          .description("2024 KEEPER CTF 입니다.")
          .isJoinable(true)
          .build();

      mockMvc.perform(put("/ctf/contests/{contestId}", ctfContest.getId())
              .content(asJsonString(request))
              .cookie(new Cookie(ACCESS_TOKEN.getTokenName(), adminToken))
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isCreated())
          .andDo(document("update-ctf-contest",
              requestCookies(
                  cookieWithName(ACCESS_TOKEN.getTokenName())
                      .description("ACCESS TOKEN %s".formatted(securedValue))
              ),
              pathParameters(
                  parameterWithName("contestId").description("대회 ID")
              ),
              requestFields(
                  fieldWithPath("name").description("CTF 대회명"),
                  fieldWithPath("description").description("CTF 대회 설명"),
                  fieldWithPath("joinable").description("CTF 대회 공개 여부")
              )));
    }
  }

  @Nested
  @DisplayName("CTF 공개 비공개 테스트")
  class CtfOpenAndCloseTest {

    @Test
    @DisplayName("유효한 요청일 경우 CTF 공개는 성공한다.")
    public void 유효한_요청일_경우_CTF_공개는_성공한다() throws Exception {
      String securedValue = getSecuredValue(CtfContestController.class, "openContest");

      CtfContest ctfContest = ctfContestTestHelper.generate();

      mockMvc.perform(patch("/ctf/contests/{contestId}/open", ctfContest.getId())
              .cookie(new Cookie(ACCESS_TOKEN.getTokenName(), adminToken)))
          .andExpect(status().isNoContent())
          .andDo(document("open-ctf-contest",
              requestCookies(
                  cookieWithName(ACCESS_TOKEN.getTokenName())
                      .description("ACCESS TOKEN %s".formatted(securedValue))
              ),
              pathParameters(
                  parameterWithName("contestId").description("대회 ID")
              )));
    }

    @Test
    @DisplayName("유효한 요청일 경우 CTF 비공개는 성공한다.")
    public void 유효한_요청일_경우_CTF_비공개는_성공한다() throws Exception {
      String securedValue = getSecuredValue(CtfContestController.class, "closeContest");

      CtfContest ctfContest = ctfContestTestHelper.generate();

      mockMvc.perform(patch("/ctf/contests/{contestId}/close", ctfContest.getId())
              .cookie(new Cookie(ACCESS_TOKEN.getTokenName(), adminToken)))
          .andExpect(status().isNoContent())
          .andDo(document("close-ctf-contest",
              requestCookies(
                  cookieWithName(ACCESS_TOKEN.getTokenName())
                      .description("ACCESS TOKEN %s".formatted(securedValue))
              ),
              pathParameters(
                  parameterWithName("contestId").description("대회 ID")
              )));
    }
  }

  @Nested
  @DisplayName("CTF 대회 목록 조회 테스트")
  class CtfContestGetTest {

    @Test
    @DisplayName("유효한 요청일 경우 CTF 대회 목록 조회는 성공한다.")
    public void 유효한_요청일_경우_CTF_대회_목록_조회는_성공한다() throws Exception {
      String securedValue = getSecuredValue(CtfContestController.class, "getContests");

      ctfContestTestHelper.generate();
      ctfContestTestHelper.generate();
      ctfContestTestHelper.generate();

      mockMvc.perform(get("/ctf/contests")
              .cookie(new Cookie(ACCESS_TOKEN.getTokenName(), adminToken)))
          .andExpect(status().isOk())
          .andDo(document("get-ctf-contests",
              requestCookies(
                  cookieWithName(ACCESS_TOKEN.getTokenName())
                      .description("ACCESS TOKEN %s".formatted(securedValue))
              ),
              queryParameters(
                  parameterWithName("page").description("페이지 (default: 0)")
                      .optional(),
                  parameterWithName("size").description("한 페이지당 불러올 개수 (default: 10)")
                      .optional()
              ),
              responseFields(
                  pageHelper(getCtfContestResponse())
              )));
    }

    FieldDescriptor[] getCtfContestResponse() {
      return new FieldDescriptor[]{
          fieldWithPath("id").description("대회 ID"),
          fieldWithPath("name").description("대회 이름"),
          fieldWithPath("description").description("대회 설명"),
          fieldWithPath("creatorName").description("대회 개최자 이름"),
          fieldWithPath("joinable").description("대회 참여 가능 여부")
      };
    }
  }
}
