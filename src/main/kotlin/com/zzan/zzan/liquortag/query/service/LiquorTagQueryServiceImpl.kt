
package com.zzan.zzan.liquortag.query.service

import com.zzan.zzan.api.liquortag.dto.LiquorTagResponse
import com.zzan.zzan.common.exception.CustomException
import com.zzan.zzan.liquor.command.repository.LiquorRepository
import com.zzan.zzan.liquortag.command.repository.LiquorTagRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LiquorTagQueryServiceImpl(
    private val liquorTagRepository: LiquorTagRepository,
    private val liquorRepository: LiquorRepository
) : LiquorTagQueryService {

    @Cacheable("liquorTags", key = "'feed:' + #feedId")
    override fun getTagsByFeedId(feedId: String): List<LiquorTagResponse> {
        val tags = liquorTagRepository.findByFeedIdOrderByImageId(feedId)
        return tags.map { convertToResponse(it) }
    }

    @Cacheable("liquorTags", key = "'image:' + #imageId")
    override fun getTagsByImageId(imageId: String): List<LiquorTagResponse> {
        val tags = liquorTagRepository.findByImageIdOrderByTagX(imageId)
        return tags.map { convertToResponse(it) }
    }

    @Cacheable("liquorTags", key = "'tag:' + #tagId")
    override fun getTagById(tagId: String): LiquorTagResponse? {
        val tag = liquorTagRepository.findById(tagId).orElse(null) ?: return null
        return convertToResponse(tag)
    }

    private fun convertToResponse(tag: com.zzan.zzan.feed.command.domain.LiquorTag): LiquorTagResponse {
        val liquor = liquorRepository.findById(tag.liquorId).orElseThrow {
            CustomException(HttpStatus.NOT_FOUND, "전통주 정보를 찾을 수 없습니다: ${tag.liquorId}")
        }

        return LiquorTagResponse(
            id = tag.id,
            feedId = tag.feedId,
            imageId = tag.imageId,
            liquorId = tag.liquorId,
            liquorName = liquor.name,
            liquorType = liquor.type,
            liquorBrewery = liquor.brewery,
            tagX = tag.tagX,
            tagY = tag.tagY
        )
    }
}
