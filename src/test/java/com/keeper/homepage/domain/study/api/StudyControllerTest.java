package com.keeper.homepage.domain.study.api;

import static com.keeper.homepage.domain.member.entity.job.MemberJob.MemberJobType.ROLE_회원;
import static com.keeper.homepage.domain.study.dto.request.StudyCreateRequest.STUDY_INFORMATION_LENGTH;
import static com.keeper.homepage.domain.study.dto.request.StudyCreateRequest.STUDY_TITLE_LENGTH;
import static com.keeper.homepage.global.config.security.data.JwtType.ACCESS_TOKEN;
import static com.keeper.homepage.global.restdocs.RestDocsHelper.field;
import static com.keeper.homepage.global.restdocs.RestDocsHelper.getSecuredValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.keeper.homepage.domain.member.entity.Member;
import com.keeper.homepage.domain.post.api.PostController;
import com.keeper.homepage.domain.study.dto.request.StudyUpdateRequest;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.snippet.Attributes.Attribute;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class StudyControllerTest extends StudyApiTestHelper {

  private MockMultipartFile thumbnail;
  private Member member, other;
  private String memberToken, otherToken;
  private long studyId;
  private final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

  @BeforeEach
  void setUp() throws IOException {
    member = memberTestHelper.builder().build();
    other = memberTestHelper.builder().build();
    thumbnail = thumbnailTestHelper.getSmallThumbnailFile();
    memberToken = jwtTokenProvider.createAccessToken(ACCESS_TOKEN, member.getId(), ROLE_회원);
    otherToken = jwtTokenProvider.createAccessToken(ACCESS_TOKEN, other.getId(), ROLE_회원);
    studyId = studyTestHelper.builder().headMember(member).build().getId();
  }

  @Nested
  @DisplayName("스터디 생성")
  class CreateStudy {

    @Test
    @DisplayName("유효한 요청 시 스터디 생성은 성공한다.")
    public void 유효한_요청_시_스터디_생성은_성공한다() throws Exception {
      String securedValue = getSecuredValue(StudyController.class, "createStudy");

      addAllParams(params);

      callCreateStudyApiWithThumbnail(memberToken, thumbnail, params)
          .andExpect(status().isCreated())
          .andDo(document("create-study",
              requestCookies(
                  cookieWithName(ACCESS_TOKEN.getTokenName())
                      .description("ACCESS TOKEN %s".formatted(securedValue) + " 스터디 생성자는 스터디장이 됩니다.")
              ),
              queryParameters(
                  parameterWithName("title")
                      .description("스터디 이름을 입력해주세요. (최대 가능 길이 : " + STUDY_TITLE_LENGTH + ")"),
                  parameterWithName("information")
                      .description("스터디 설명을 입력해주세요. (최대 가능 길이 : " + STUDY_INFORMATION_LENGTH + ")"),
                  parameterWithName("year")
                      .description("스터디 년도를 입력해주세요."),
                  parameterWithName("season")
                      .attributes(new Attribute("format", "1: 1학기 2: 여름학기 3: 2학기 4: 겨울학기"))
                      .description("스터디 학기를 입력해주세요."),
                  parameterWithName("gitLink")
                      .attributes(new Attribute("format", "\"https://github.com\"으로 시작"))
                      .description("스터디 깃허브 링크를 입력해주세요.").optional(),
                  parameterWithName("noteLink")
                      .attributes(new Attribute("format", "\"https://www.notion.so\"으로 시작"))
                      .description("스터디 노트 링크를 입력해주세요.").optional(),
                  parameterWithName("etcLink")
                      .description("스터디 기타 링크를 입력해주세요.").optional()
              ),
              requestParts(
                  partWithName("thumbnail").description("스터디의 썸네일")
                      .optional()
              )));
    }

    private void addAllParams(MultiValueMap<String, String> params) {
      params.add("title", "자바 스터디");
      params.add("information", "자바 스터디 입니다");
      params.add("year", "2023");
      params.add("season", "1");
      params.add("gitLink", "https://github.com/KEEPER31337/Homepage-Back-R2");
      params.add("noteLink", "https://www.notion.so/Java-Spring");
      params.add("etcLink", "etc.com");
    }

    @Test
    @DisplayName("깃허브 링크가 아닌 링크를 입력할 경우 스터디 생성은 실패한다.")
    public void 깃허브_링크가_아닌_링크를_입력할_경우_스터디_생성은_실패한다() throws Exception {
      params.add("title", "자바 스터디");
      params.add("information", "자바 스터디 입니다");
      params.add("year", "2023");
      params.add("season", "1");
      params.add("gitLink", "https://www.youtube.com/");
      params.add("noteLink", "https://www.notion.so/Java-Spring");
      params.add("etcLink", "etc.com");

      callCreateStudyApiWithThumbnail(memberToken, thumbnail, params)
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("노션 링크가 아닌 링크를 입력할 경우 스터디 생성은 실패한다.")
    public void 노션_링크가_아닌_링크를_입력할_경우_스터디_생성은_실패한다() throws Exception {
      params.add("title", "자바 스터디");
      params.add("information", "자바 스터디 입니다");
      params.add("year", "2023");
      params.add("season", "1");
      params.add("gitLink", "https://github.com/KEEPER31337/Homepage-Back-R2");
      params.add("notionLink", "https://www.youtube.com/");
      params.add("etcLink", "etc.com");

      callCreateStudyApiWithThumbnail(memberToken, thumbnail, params)
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("스터디 삭제")
  class DeleteStudy {

    @Test
    @DisplayName("유효한 요청 시 스터디 삭제는 성공한다.")
    public void 유효한_요청_시_스터디_삭제는_성공한다() throws Exception {
      String securedValue = getSecuredValue(StudyController.class, "deleteStudy");

      callDeleteStudyApi(memberToken, studyId)
          .andExpect(status().isNoContent())
          .andDo(document("delete-study",
              requestCookies(
                  cookieWithName(ACCESS_TOKEN.getTokenName())
                      .description("ACCESS TOKEN %s".formatted(securedValue))
              ),
              pathParameters(
                  parameterWithName("studyId")
                      .description("삭제하고자 하는 스터디의 ID")
              )));
    }

    @Test
    @DisplayName("스터디장이 아닐 경우 스터디 삭제는 실패한다.")
    public void 스터디장이_아닐_경우_스터디_삭제는_실패한다() throws Exception {
      callDeleteStudyApi(otherToken, studyId)
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("스터디 조회")
  class GetStudy {

    @BeforeEach
    void setUp() {
      studyTestHelper.builder().year(2023).season(1).build();
    }

    @Test
    @DisplayName("스터디 조회는 성공해야 한다.")
    public void 스터디_조회는_성공해야_한다() throws Exception {
      String securedValue = getSecuredValue(StudyController.class, "getStudy");

      callGetStudyApi(memberToken, studyId)
          .andExpect(status().isOk())
          .andDo(document("get-study",
              requestCookies(
                  cookieWithName(ACCESS_TOKEN.getTokenName())
                      .description("ACCESS TOKEN %s".formatted(securedValue))
              ),
              pathParameters(
                  parameterWithName("studyId").description("조회하고자 하는 스터디의 ID")
              ),
              responseFields(
                  fieldWithPath("information").description("스터디 정보"),
                  fieldWithPath("members[]").description("스터디원 실명 리스트"),
                  fieldWithPath("gitLink").description("스터디 깃허브 링크 주소"),
                  fieldWithPath("notionLink").description("스터디 노트(노션) 링크 주소"),
                  fieldWithPath("etcLink").description("스터디 기타 링크 주소")
              )));
    }

    @Test
    @DisplayName("스터디 목록 조회는 성공해야 한다.")
    public void 스터디_목록_조회는_성공해야_한다() throws Exception {
      String securedValue = getSecuredValue(StudyController.class, "getStudies");

      callGetStudiesApi(memberToken, 2023, 1)
          .andExpect(status().isOk())
          .andDo(document("get-studies",
              requestCookies(
                  cookieWithName(ACCESS_TOKEN.getTokenName())
                      .description("ACCESS TOKEN %s".formatted(securedValue))
              ),
              queryParameters(
                  parameterWithName("year").description("조회하고자 하는 스터디 년도"),
                  parameterWithName("season").description("조회하고자 하는 스터디 학기")
              ),
              responseFields(
                  fieldWithPath("studies[].studyId").description("스터디 ID"),
                  fieldWithPath("studies[].thumbnailPath").description("스터디 썸네일 경로"),
                  fieldWithPath("studies[].title").description("스터디 이름"),
                  fieldWithPath("studies[].headName").description("스터디장 이름 (실명)"),
                  fieldWithPath("studies[].memberCount").description("스터디원 수")
              )));
    }
  }

  @Nested
  @DisplayName("스터디 수정")
  class UpdateStudy {

    @Test
    @DisplayName("유효한 요청일 경우 스터디 수정은 성공한다.")
    public void 유효한_요청일_경우_스터디_수정은_성공한다() throws Exception {
      String securedValue = getSecuredValue(StudyController.class, "updateStudy");

      StudyUpdateRequest request = StudyUpdateRequest.builder()
          .title("자바 스터디")
          .information("자바 스터디 입니다.")
          .year(2023)
          .season(2)
          .gitLink("https://github.com/KEEPER31337/Homepage-Back-R2")
          .build();

      callUpdateStudyApi(memberToken, studyId, request)
          .andExpect(status().isCreated())
          .andDo(document("update-study",
              requestCookies(
                  cookieWithName(ACCESS_TOKEN.getTokenName())
                      .description("ACCESS TOKEN %s".formatted(securedValue))
              ),
              pathParameters(
                  parameterWithName("studyId").description("스터디 ID")
              ),
              requestFields(
                  field("title", "스터디 제목 (최대 가능 길이 : " + STUDY_TITLE_LENGTH + ")"),
                  field("information", "저자 (최대 가능 길이 : " + STUDY_INFORMATION_LENGTH + ")"),
                  field("year", "스터디 년도"),
                  field("season", "스터디 학기")
                      .attributes(new Attribute("format", "1: 1학기 2: 여름학기 3: 2학기 4: 겨울학기")),
                  field("gitLink", "깃허브 링크")
                      .attributes(new Attribute("format", "\"https://github.com\"으로 시작"))
                      .optional(),
                  field("notionLink", "노션 링크")
                      .attributes(new Attribute("format", "\"https://www.notion.so\"으로 시작"))
                      .optional(),
                  field("etcLink", "기타 링크")
                      .optional()
              ),
              responseHeaders(
                  headerWithName("Location").description("수정한 스터디를 불러오는 URI 입니다.")
              )));
    }

    @Test
    @DisplayName("유효한 요청일 경우 스터디 썸네일 수정은 성공한다.")
    public void 유효한_요청일_경우_스터디_썸네일_수정은_성공한다() throws Exception {
      String securedValue = getSecuredValue(StudyController.class, "updateStudyThumbnail");
      MockMultipartFile newThumbnailFile = thumbnailTestHelper.getThumbnailFile();

      callUpdateStudyThumbnailApi(memberToken, studyId, newThumbnailFile)
          .andExpect(status().isNoContent())
          .andDo(document("update-study-thumbnail",
                  requestCookies(
                      cookieWithName(ACCESS_TOKEN.getTokenName())
                          .description("ACCESS TOKEN %s".formatted(securedValue))
                  ),
                  pathParameters(
                      parameterWithName("studyId").description("스터디 ID")
                  ),
                  requestParts(
                      partWithName("thumbnail").description("책의 썸네일 (null 값으로 보낼 경우 기본 썸네일로 지정됩니다.)")
                          .optional()
                  )
              )
          );
    }

    @Test
    @DisplayName("스터디장이 아닐 경우 스터디 수정은 실패한다.")
    public void 스터디장이_아닐_경우_스터디_수정은_실패한다() throws Exception {
      MockMultipartFile newThumbnailFile = thumbnailTestHelper.getThumbnailFile();
      callUpdateStudyThumbnailApi(otherToken, studyId, newThumbnailFile)
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("스터디원 추가")
  class AddStudyMember {

    @Test
    @DisplayName("유효한 요청일 경우 스터디원 추가는 성공한다.")
    public void 유효한_요청일_경우_스터디원_추가는_성공한다() throws Exception {
      String securedValue = getSecuredValue(StudyController.class, "joinStudy");

      callJoinStudyApi(memberToken, studyId, other.getId())
          .andExpect(status().isCreated())
          .andDo(document("join-study",
                  requestCookies(
                      cookieWithName(ACCESS_TOKEN.getTokenName())
                          .description("ACCESS TOKEN %s".formatted(securedValue))
                  ),
                  pathParameters(
                      parameterWithName("studyId").description("스터디 ID"),
                      parameterWithName("memberId").description("회원 ID")
                  )
              )
          );
    }
  }

  @Nested
  @DisplayName("스터디원 삭제")
  class DeleteStudyMember {

    @Test
    @DisplayName("유효한 요청일 경우 스터디원 삭제는 성공한다.")
    public void 유효한_요청일_경우_스터디원_삭제는_성공한다() throws Exception {
      String securedValue = getSecuredValue(StudyController.class, "leaveStudy");

      callLeaveStudyApi(memberToken, studyId, other.getId())
          .andExpect(status().isNoContent())
          .andDo(document("leave-study",
                  requestCookies(
                      cookieWithName(ACCESS_TOKEN.getTokenName())
                          .description("ACCESS TOKEN %s".formatted(securedValue))
                  ),
                  pathParameters(
                      parameterWithName("studyId").description("스터디 ID"),
                      parameterWithName("memberId").description("회원 ID")
                  )
              )
          );
    }
  }
}
