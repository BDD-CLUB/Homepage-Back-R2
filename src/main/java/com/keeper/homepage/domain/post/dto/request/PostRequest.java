package com.keeper.homepage.domain.post.dto.request;

import static com.keeper.homepage.domain.post.entity.Post.MAX_PASSWORD_LENGTH;
import static com.keeper.homepage.domain.post.entity.Post.MAX_TITLE_LENGTH;
import static lombok.AccessLevel.PRIVATE;

import com.keeper.homepage.domain.member.entity.Member;
import com.keeper.homepage.domain.post.entity.Post;
import com.keeper.homepage.domain.post.entity.category.Category;
import com.keeper.homepage.domain.thumbnail.entity.Thumbnail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class PostRequest {

  public static final int MAX_REQUEST_TITLE_LENGTH = 50;
  public static final int MAX_REQUEST_PASSWORD_LENGTH = 16;

  @NotBlank(message = "게시글 제목을 입력해주세요.")
  @Length(max = MAX_REQUEST_TITLE_LENGTH, message = "게시글 제목은 " + MAX_REQUEST_TITLE_LENGTH
      + "자 이하로 입력해주세요.")
  private String title;

  @NotBlank(message = "게시글 본문을 입력해주세요.")
  private String content;

  @Nullable
  private Boolean allowComment;

  @Nullable
  private Boolean isNotice;

  @Nullable
  private Boolean isSecret;

  @Nullable
  private Boolean isTemp;

  @Nullable
  @Length(max = MAX_REQUEST_PASSWORD_LENGTH, message = "비밀번호는 " + MAX_REQUEST_PASSWORD_LENGTH
      + "자 이하로 입력해주세요.")
  private String password;

  @NotNull(message = "카테고리 아이디를 입력해주세요.")
  @PositiveOrZero(message = "올바른 카테고리 아이디를 입력해주세요.")
  private Long categoryId;

  private MultipartFile thumbnail;
  private List<MultipartFile> files;

  public Post toEntity(Member member, String ipAddress) {
    return Post.builder()
        .title(title)
        .content(content)
        .member(member)
        .ipAddress(ipAddress)
        .allowComment(allowComment)
        .isNotice(isNotice)
        .isSecret(isSecret)
        .isTemp(isTemp)
        .password(password)
        .build();
  }
}
