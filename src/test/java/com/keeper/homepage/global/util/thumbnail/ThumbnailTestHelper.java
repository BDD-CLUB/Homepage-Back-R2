package com.keeper.homepage.global.util.thumbnail;

import com.keeper.homepage.domain.thumbnail.dao.ThumbnailRepository;
import com.keeper.homepage.domain.thumbnail.entity.Thumbnail;
import com.keeper.homepage.global.util.thumbnail.server.ThumbnailServerUtil;
import java.io.FileInputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

@Component
public class ThumbnailTestHelper {

  @Autowired
  ThumbnailRepository thumbnailRepository;

  public Thumbnail generateThumbnail() {
    final ThumbnailUtil thumbnailUtil = new ThumbnailServerUtil(thumbnailRepository);
    return thumbnailUtil.save(getThumbnailFile()).orElseThrow();
  }

  public MockMultipartFile getThumbnailFile() {
    try {
      return new MockMultipartFile("thumbnail",
          "testImage_210x210.png", "image/png",
          new FileInputStream("src/test/resources/images/testImage_210x210.png"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public MockMultipartFile getFakeImageFile() {
    try {
      return new MockMultipartFile("thumbnail",
          "fakeImage.png", "image/png",
          new FileInputStream("src/test/resources/images/fakeImage.png"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
