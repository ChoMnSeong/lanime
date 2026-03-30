package com.ensnif.lanime.domain.episode.controller

import com.ensnif.lanime.domain.social.dto.CommentRequest
import com.ensnif.lanime.domain.social.dto.CommentResponse
import com.ensnif.lanime.domain.social.dto.CommentUpdateRequest
import com.ensnif.lanime.domain.social.service.CommentService
import com.ensnif.lanime.global.common.dto.ApiResponse
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/episodes/{episodeId}/comments")
class EpisodeCommentController(
    private val commentService: CommentService
) {

    @PostMapping
    fun createComment(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable episodeId: UUID,
        @Valid @RequestBody request: CommentRequest
    ): Mono<ApiResponse<Unit>> {
        val profileId = context.profileId ?: throw BusinessException(ErrorCode.FORBIDDEN)

        return commentService.createComment(episodeId, profileId, request)
            .thenReturn(ApiResponse.withMessage("댓글이 등록되었습니다."))
    }

    @GetMapping
    fun getComments(
        @PathVariable episodeId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): Mono<ApiResponse<Map<String, Any>>> {
        return commentService.getComments(episodeId, page, limit)
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/{commentId}/replies")
    fun getReplies(
        @PathVariable episodeId: UUID,
        @PathVariable commentId: UUID
    ): Mono<ApiResponse<List<CommentResponse>>> {
        return commentService.getReplies(commentId)
            .map { ApiResponse.success(it) }
    }

    @PatchMapping("/{commentId}")
    fun updateComment(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable episodeId: UUID,
        @PathVariable commentId: UUID,
        @Valid @RequestBody request: CommentUpdateRequest
    ): Mono<ApiResponse<Unit>> {
        val profileId = context.profileId ?: throw BusinessException(ErrorCode.FORBIDDEN)

        return commentService.updateComment(commentId, profileId, request)
            .thenReturn(ApiResponse.withMessage("댓글이 수정되었습니다."))
    }

    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable episodeId: UUID,
        @PathVariable commentId: UUID
    ): Mono<ApiResponse<Unit>> {
        val profileId = context.profileId ?: throw BusinessException(ErrorCode.FORBIDDEN)

        return commentService.deleteComment(commentId, profileId)
            .thenReturn(ApiResponse.withMessage("댓글이 삭제되었습니다."))
    }
}
