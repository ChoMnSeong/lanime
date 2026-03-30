package com.ensnif.lanime.domain.social.service

import com.ensnif.lanime.domain.social.dto.CommentRequest
import com.ensnif.lanime.domain.social.dto.CommentResponse
import com.ensnif.lanime.domain.social.dto.CommentUpdateRequest
import com.ensnif.lanime.domain.social.entity.EpisodeComment
import com.ensnif.lanime.domain.social.repository.EpisodeCommentRepository
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class CommentService(
    private val commentRepository: EpisodeCommentRepository
) {

    @Transactional
    fun createComment(episodeId: UUID, profileId: UUID, request: CommentRequest): Mono<Unit> {
        val saveComment = {
            commentRepository.save(
                EpisodeComment(
                    episodeId = episodeId,
                    profileId = profileId,
                    content = request.content,
                    parentCommentId = request.parentCommentId
                )
            ).thenReturn(Unit)
        }

        // 대댓글인 경우 부모 댓글 존재 여부 확인
        return if (request.parentCommentId != null) {
            commentRepository.findById(request.parentCommentId)
                .switchIfEmpty(Mono.error(BusinessException(ErrorCode.COMMENT_NOT_FOUND)))
                .flatMap { parent ->
                    // 대댓글에 대댓글 방지 (2단계까지만 허용)
                    if (parent.parentCommentId != null) {
                        Mono.error(BusinessException(ErrorCode.INVALID_INPUT_VALUE))
                    } else {
                        saveComment()
                    }
                }
        } else {
            saveComment()
        }
    }

    fun getComments(episodeId: UUID, page: Int, limit: Int): Mono<Map<String, Any>> {
        val offset = (page * limit).toLong()
        return commentRepository.countByEpisodeIdAndParentCommentIdIsNull(episodeId)
            .flatMap { total ->
                commentRepository.findRootComments(episodeId, limit, offset)
                    .collectList()
                    .map { comments ->
                        mapOf(
                            "comments" to comments,
                            "total" to total,
                            "page" to page,
                            "limit" to limit,
                            "totalPages" to Math.ceil(total.toDouble() / limit).toInt()
                        )
                    }
            }
    }

    fun getReplies(parentCommentId: UUID): Mono<List<CommentResponse>> {
        return commentRepository.findById(parentCommentId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.COMMENT_NOT_FOUND)))
            .flatMap { commentRepository.findReplies(parentCommentId).collectList() }
    }

    @Transactional
    fun updateComment(commentId: UUID, profileId: UUID, request: CommentUpdateRequest): Mono<Unit> {
        return commentRepository.findById(commentId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.COMMENT_NOT_FOUND)))
            .flatMap { comment ->
                if (comment.profileId != profileId) {
                    Mono.error(BusinessException(ErrorCode.FORBIDDEN))
                } else {
                    commentRepository.save(
                        comment.copy(content = request.content)
                            .apply { createdAt = comment.createdAt }
                    ).thenReturn(Unit)
                }
            }
    }

    @Transactional
    fun deleteComment(commentId: UUID, profileId: UUID): Mono<Unit> {
        return commentRepository.findById(commentId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.COMMENT_NOT_FOUND)))
            .flatMap { comment ->
                if (comment.profileId != profileId) {
                    Mono.error(BusinessException(ErrorCode.FORBIDDEN))
                } else {
                    commentRepository.deleteById(commentId).thenReturn(Unit)
                }
            }
    }
}
