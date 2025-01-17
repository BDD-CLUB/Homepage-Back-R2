package com.keeper.homepage.domain.library.dao;

import com.keeper.homepage.domain.library.entity.BookBorrowInfo;
import com.keeper.homepage.domain.library.entity.BookBorrowLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookBorrowLogRepository extends JpaRepository<BookBorrowLog, Long> {

  @Query(value = "select borrowLog "
      + "from BookBorrowLog borrowLog "
      + "where (borrowLog.bookTitle like %:search% " // 도서명
      + "or borrowLog.bookAuthor like %:search% " // 저자
      + "or borrowLog.memberRealName like %:search%) " // 이름
      + "order by borrowLog.id desc " // 최신순
  )
  Page<BookBorrowLog> findAll(@Param("search") String search, Pageable pageable);

  @Query(value = "select borrowLog "
      + "from BookBorrowLog borrowLog "
      + "where borrowLog.borrowStatus = :borrowStatus "
      + "and (borrowLog.bookTitle like %:search% " // 도서명
      + "or borrowLog.bookAuthor like %:search% " // 저자
      + "or borrowLog.memberRealName like %:search%) " // 이름
      + "order by borrowLog.id desc " // 최신순
  )
  Page<BookBorrowLog> findAllByStatus(@Param("borrowStatus") String borrowStatus,
      @Param("search") String search, Pageable pageable);

}
