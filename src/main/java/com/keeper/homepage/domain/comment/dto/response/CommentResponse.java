package com.keeper.homepage.domain.comment.dto.response;

import com.keeper.homepage.domain.comment.entity.Comment;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponse {

  private String writerName;
  private String writerThumbnailPath;
  private String content;
  private LocalDateTime registerTime;
  private Long parentId;
  private Integer likeCount;
  private Integer dislikeCount;

  public static CommentResponse of(Comment comment, String writerThumbnailPath) {
    return CommentResponse.builder()
        .writerName(comment.getMember().getNickname())
        .writerThumbnailPath(writerThumbnailPath)
        .content(comment.getContent())
        .registerTime(comment.getRegisterTime())
        .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
        .likeCount(comment.getCommentLikes().size())
        .dislikeCount(comment.getCommentDislikes().size())
        .build();
  }
}