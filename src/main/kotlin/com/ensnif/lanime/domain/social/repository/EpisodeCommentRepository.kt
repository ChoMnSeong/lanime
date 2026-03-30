package com.ensnif.lanime.domain.social.repository

import com.ensnif.lanime.domain.social.dto.CommentResponse
import com.ensnif.lanime.domain.social.entity.EpisodeComment
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface EpisodeCommentRepository : ReactiveCrudRepository<EpisodeComment, UUID> {

    @Query("""
        SELECT c.comment_id, c.profile_id, c.content, c.parent_comment_id, c.created_at, c.updated_at,
               p.name AS profile_name, p.avatar_url,
               COUNT(r.comment_id) AS reply_count
        FROM episode_comment c
        JOIN user_profile p ON c.profile_id = p.profile_id
        LEFT JOIN episode_comment r ON r.parent_comment_id = c.comment_id
        WHERE c.episode_id = :episodeId AND c.parent_comment_id IS NULL
        GROUP BY c.comment_id, p.name, p.avatar_url
        ORDER BY c.created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    fun findRootComments(episodeId: UUID, limit: Int, offset: Long): Flux<CommentResponse>

    @Query("""
        SELECT c.comment_id, c.profile_id, c.content, c.parent_comment_id, c.created_at, c.updated_at,
               p.name AS profile_name, p.avatar_url,
               0 AS reply_count
        FROM episode_comment c
        JOIN user_profile p ON c.profile_id = p.profile_id
        WHERE c.parent_comment_id = :parentCommentId
        ORDER BY c.created_at ASC
    """)
    fun findReplies(parentCommentId: UUID): Flux<CommentResponse>

    fun countByEpisodeIdAndParentCommentIdIsNull(episodeId: UUID): Mono<Long>
}
