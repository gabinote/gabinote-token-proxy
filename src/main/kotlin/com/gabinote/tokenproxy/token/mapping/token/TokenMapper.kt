package com.gabinote.tokenproxy.token.mapping.token

import com.gabinote.tokenproxy.token.dto.token.controller.TokenExchangeReqControllerDto
import com.gabinote.tokenproxy.token.dto.token.controller.TokenResControllerDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeResServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenRefreshResServiceDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * Token 관련 Mapper
 */
@Mapper(
    componentModel = "spring"
)
interface TokenMapper {

    /**
     * TokenExchangeResServiceDto -> TokenResControllerDto 변환
     * 이때 오직 Access 토큰과 만료 시기만 매핑
     * @param dto TokenExchangeResServiceDto
     * @return TokenResControllerDto
     */
    @Mapping(source = "accessTokenExpiresIn", target = "expiresIn")
    fun toExchangeResControllerDto(dto: TokenExchangeResServiceDto) : TokenResControllerDto

    /**
     * TokenRefreshResServiceDto -> TokenResControllerDto 변환
     * 이때 오직 Access 토큰과 만료 시기만 매핑
     * @param dto TokenRefreshResServiceDto
     * @return TokenResControllerDto
     */
    @Mapping(source = "accessTokenExpiresIn", target = "expiresIn")
    fun toRefreshResControllerDto(dto: TokenRefreshResServiceDto) : TokenResControllerDto

    /**
     * TokenExchangeReqControllerDto -> TokenExchangeReqServiceDto 변환
     * @param dto TokenExchangeReqControllerDto
     * @return TokenExchangeReqServiceDto
     */
    fun toExchangeReqServiceDto(dto: TokenExchangeReqControllerDto) : TokenExchangeReqServiceDto


}