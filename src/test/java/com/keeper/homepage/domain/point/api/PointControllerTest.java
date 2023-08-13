package com.keeper.homepage.domain.point.api;

import static com.keeper.homepage.domain.member.entity.job.MemberJob.MemberJobType.*;
import static com.keeper.homepage.global.config.security.data.JwtType.*;
import static com.keeper.homepage.global.restdocs.RestDocsHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.cookies.CookieDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.keeper.homepage.IntegrationTest;
import com.keeper.homepage.domain.member.entity.Member;
import com.keeper.homepage.domain.member.entity.job.MemberJob.MemberJobType;
import com.keeper.homepage.domain.point.dto.request.presentPointRequest;
import com.keeper.homepage.global.config.security.data.JwtType;
import com.keeper.homepage.global.restdocs.RestDocsHelper;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Payload;
import javax.print.DocFlavor.STRING;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.cookies.CookieDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.PayloadDocumentation;

class PointControllerTest extends IntegrationTest {

  private Member member, otherMember;
  private String memberAccessToken;
  private static final int GIVEPOINT = 1000;
  private static final String GIVEMESSAGE = "TEST MESSAGE";

  @Nested
  @DisplayName("포인트 선물 API 테스트")
  class presentPointTest {

    @BeforeEach
    void setUp() {
      member = memberTestHelper.generate();
      otherMember = memberTestHelper.generate();
      memberAccessToken = jwtTokenProvider.createAccessToken(ACCESS_TOKEN, member.getId(), ROLE_회원);
    }

    @Test
    @DisplayName("포인트 선물을 성공해야 한다.")
    void 포인트_선물을_성공해야_한다() throws Exception {
      String securedValue = getSecuredValue(PointController.class, "presentPoint");
      presentPointRequest request = presentPointRequest.builder()
          .point(GIVEPOINT)
          .message(GIVEMESSAGE)
          .memberId(otherMember.getId())
          .build();
      mockMvc.perform(post("/points/present")
              .cookie(new Cookie(ACCESS_TOKEN.getTokenName(), memberAccessToken))
              .content(asJsonString(request))
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isCreated())
          .andDo(document("create-presentPointLog",
              requestCookies(
                  cookieWithName(ACCESS_TOKEN.getTokenName())
                      .description("ACCESS TOKEN %s".formatted(securedValue))
              ),
              requestFields(
                  fieldWithPath("point").description("선물할 점수를 입력해주세요."),
                  fieldWithPath("memberId").description("선물받을 회원의 ID를 입력해주세요."),
                  fieldWithPath("message").description("선물받을 회원에게 보낼 메시지를 입력해주세요")
              )));
    }
  }
}
