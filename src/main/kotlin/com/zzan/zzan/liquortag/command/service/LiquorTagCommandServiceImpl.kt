package com.zzan.zzan.liquortag.command.service

import com.zzan.zzan.api.liquortag.dto.CreateLiquorTagRequest
import com.zzan.zzan.api.liquortag.dto.TagInFeedRequest
import com.zzan.zzan.common.exception.CustomException
import com.zzan.zzan.feed.command.domain.FeedImage
import com.zzan.zzan.feed.command.domain.LiquorTag
import com.zzan.zzan.feed.command.repository.FeedImageRepository
import com.zzan.zzan.liquor.command.repository.LiquorRepository
import com.zzan.zzan.liquortag.command.repository.LiquorTagRepository
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LiquorTagCommandServiceImpl(
    private val liquorTagRepository: LiquorTagRepository,
    private val feedImageRepository: FeedImageRepository,
    private val liquorRepository: LiquorRepository
) : LiquorTagCommandService {

    companion object : KLogging() {
        private const val MAX_TAGS_PER_IMAGE = 5
    }

    override fun createTag(request: CreateLiquorTagRequest): String {
        // 검증
        validateImageExists(request.imageId)
        validateLiquorExists(request.liquorId)
        validateTagLimit(request.imageId)
        validateCoordinates(request.tagX, request.tagY)

        // 태그 생성
        val tag = LiquorTag(
            feedId = request.feedId,
            imageId = request.imageId,
            liquorId = request.liquorId,
            tagX = request.tagX,
            tagY = request.tagY
        )

        val savedTag = liquorTagRepository.save(tag)
        logger.info("Created liquor tag: ${savedTag.id}")

        return savedTag.id
    }

    override fun createTagsForFeed(feedId: String, images: List<FeedImage>, tagRequests: List<TagInFeedRequest>) {
        tagRequests.forEach { tagRequest ->
            val targetImage = images.find { it.orderNum == tagRequest.imageOrderNum }
                ?: throw CustomException(HttpStatus.BAD_REQUEST, "존재하지 않는 이미지 순서: ${tagRequest.imageOrderNum}")

            validateLiquorExists(tagRequest.liquorId)
            validateTagLimit(targetImage.id)
            validateCoordinates(tagRequest.tagX, tagRequest.tagY)

            val tag = LiquorTag(
                feedId = feedId,
                imageId = targetImage.id,
                liquorId = tagRequest.liquorId,
                tagX = tagRequest.tagX,
                tagY = tagRequest.tagY
            )

            liquorTagRepository.save(tag)
        }

        logger.info("Created ${tagRequests.size} tags for feed: $feedId")
    }

    override fun deleteTag(tagId: String) {
        val tag = liquorTagRepository.findById(tagId).orElseThrow {
            CustomException(HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다.")
        }
        liquorTagRepository.delete(tag)
        logger.info("Deleted tag: $tagId")
    }

    override fun deleteTagsByFeedId(feedId: String) {
        liquorTagRepository.deleteByFeedId(feedId)
        logger.info("Deleted all tags for feed: $feedId")
    }

    override fun deleteTagsByImageId(imageId: String) {
        liquorTagRepository.deleteByImageId(imageId)
        logger.info("Deleted all tags for image: $imageId")
    }

    private fun validateImageExists(imageId: String) {
        if (!feedImageRepository.existsById(imageId)) {
            throw CustomException(HttpStatus.BAD_REQUEST, "존재하지 않는 이미지입니다.")
        }
    }

    private fun validateLiquorExists(liquorId: String) {
        if (!liquorRepository.existsById(liquorId)) {
            throw CustomException(HttpStatus.BAD_REQUEST, "존재하지 않는 전통주입니다.")
        }
    }

    private fun validateTagLimit(imageId: String) {
        val count = liquorTagRepository.countByImageId(imageId)
        if (count >= MAX_TAGS_PER_IMAGE) {
            throw CustomException(HttpStatus.BAD_REQUEST, "이미지당 최대 ${MAX_TAGS_PER_IMAGE}개의 태그만 추가 가능합니다.")
        }
    }

    private fun validateCoordinates(tagX: Double, tagY: Double) {
        if (tagX < 0.0 || tagX > 1.0 || tagY < 0.0 || tagY > 1.0) {
            throw CustomException(HttpStatus.BAD_REQUEST, "태그 좌표는 0.0~1.0 사이여야 합니다.")
        }
    }
}
