package com.keeper.homepage.domain.library.application

import com.keeper.homepage.domain.library.dao.BookBorrowInfoRepository
import com.keeper.homepage.domain.library.dto.resp.BorrowResponse
import com.keeper.homepage.domain.library.entity.BookBorrowStatus.BookBorrowStatusType.*
import com.keeper.homepage.domain.library.entity.BookBorrowStatus.getBookBorrowStatusBy
import com.keeper.homepage.global.error.BusinessException
import com.keeper.homepage.global.error.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

fun BookBorrowInfoRepository.getBorrowById(borrowId: Long) = this.findById(borrowId)
    .orElseThrow { throw BusinessException(borrowId, "borrowId", ErrorCode.BORROW_NOT_FOUND) }

@Service
@Transactional(readOnly = true)
class BorrowManageService(
    val borrowInfoRepository: BookBorrowInfoRepository
) {
    fun getBorrowRequests(pageable: Pageable): Page<BorrowResponse> =
        borrowInfoRepository.findAllByBorrowStatus(getBookBorrowStatusBy(대출대기중), pageable)
            .map(::BorrowResponse)

    @Transactional
    fun approveBorrow(borrowId: Long) {
        val borrowInfo = borrowInfoRepository.getBorrowById(borrowId)
        val book = borrowInfo.book
        if (borrowInfo.borrowStatus.type != 대출대기중) {
            throw BusinessException(borrowId, "borrowId", ErrorCode.BORROW_STATUS_IS_NOT_REQUESTS)
        }
        book.borrow()
        borrowInfo.changeBorrowStats(대출승인)
    }

    @Transactional
    fun denyBorrow(borrowId: Long) {
        val borrowInfo = borrowInfoRepository.getBorrowById(borrowId)
        if (borrowInfo.borrowStatus.type != 대출대기중) {
            throw BusinessException(borrowId, "borrowId", ErrorCode.BORROW_STATUS_IS_NOT_REQUESTS)
        }
        borrowInfo.changeBorrowStats(대출거부)
    }
}