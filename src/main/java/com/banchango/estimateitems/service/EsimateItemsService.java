package com.banchango.estimateitems.service;

import com.banchango.auth.token.JwtTokenUtil;
import com.banchango.common.exception.ForbiddenException;
import com.banchango.domain.estimates.Estimates;
import com.banchango.domain.estimates.EstimatesRepository;
import com.banchango.estimateitems.dto.EstimateItemSearchDto;
import com.banchango.estimates.exception.EstimateNoContentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EsimateItemsService {
    private final EstimatesRepository estimatesRepository;

    public List<EstimateItemSearchDto> getEstimateItemsByEstimateId(String accessToken, Integer estimateId) {
        Integer userId = JwtTokenUtil.extractUserId(accessToken);

        Estimates estimate = estimatesRepository.findById(estimateId).orElseThrow(EstimateNoContentException::new);

        // estimateId 주인이 userId인지 검사
        if(!estimate.getUserId().equals(userId)) throw new ForbiddenException();

        return estimate.getEstimateItems().stream()
                .map(estimateItem -> new EstimateItemSearchDto(estimateItem))
                .collect(Collectors.toList());
    }
}
