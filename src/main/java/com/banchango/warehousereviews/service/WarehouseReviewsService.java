package com.banchango.warehousereviews.service;

import com.banchango.auth.exception.AuthenticateException;
import com.banchango.auth.token.JwtTokenUtil;
import com.banchango.domain.users.Users;
import com.banchango.domain.users.UsersRepository;
import com.banchango.domain.warehousereviews.WarehouseReviews;
import com.banchango.domain.warehousereviews.WarehouseReviewsRepository;
import com.banchango.domain.warehouses.WarehousesRepository;
import com.banchango.tools.ObjectMaker;
import com.banchango.warehousereviews.dto.WarehouseReviewInsertRequestDto;
import com.banchango.warehousereviews.dto.WarehouseReviewResponseDto;
import com.banchango.warehousereviews.exception.WarehouseReviewNotFoundException;
import com.banchango.warehouses.exception.WarehouseIdNotFoundException;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

@RequiredArgsConstructor
@Service
public class WarehouseReviewsService {

    private final WarehouseReviewsRepository reviewsRepository;
    private final WarehousesRepository warehousesRepository;
    private final UsersRepository usersRepository;

    public JSONObject getWarehouseReviewsById(Integer warehouseId, Integer limit, Integer offset, String token) throws Exception {
        if(!JwtTokenUtil.isTokenValidated(JwtTokenUtil.getToken(token))) {
            throw new AuthenticateException();
        }
        if(!warehousesRepository.findById(warehouseId).isPresent()) throw new WarehouseIdNotFoundException();
        PageRequest request = PageRequest.of(limit, offset);
        List<WarehouseReviewResponseDto> list = reviewsRepository.findByWarehouseId(warehouseId, request).stream().map(WarehouseReviewResponseDto::new).collect(Collectors.toList());
        if(list.size() == 0) throw new WarehouseReviewNotFoundException();
        JSONObject jsonObject = ObjectMaker.getJSONObject();
        JSONArray jsonArray = ObjectMaker.getJSONArray();
        for(WarehouseReviewResponseDto dto : list) {
            JSONObject reviewObject = dto.toJSONObject();
            Users user = usersRepository.findById(dto.getUserId()).orElseThrow(Exception::new);
            reviewObject.put("writer", user.toJSONObject());
            jsonArray.put(reviewObject);
        }
        jsonObject.put("reviews", jsonArray);
        return jsonObject;
    }

    public JSONObject register(Integer warehouseId, WarehouseReviewInsertRequestDto dto, String token) throws Exception {
        if(!JwtTokenUtil.isTokenValidated(JwtTokenUtil.getToken(token))) {
            throw new AuthenticateException();
        }
        if(!warehousesRepository.findById(warehouseId).isPresent()) {
            throw new WarehouseIdNotFoundException();
        }
        int userId = Integer.parseInt(JwtTokenUtil.extractUserId(JwtTokenUtil.getToken(token)));
        WarehouseReviews review = WarehouseReviews.builder()
                .rating(dto.getRating())
                .content(dto.getContent())
                .userId(userId)
                .warehouseId(warehouseId).build();
        WarehouseReviews savedReview = reviewsRepository.save(review);
        JSONObject jsonObject = ObjectMaker.getJSONObject();
        jsonObject.put("review", savedReview.toJSONObject());
        jsonObject.put("writer", ObjectMaker.getJSONObjectWithUserInfo(usersRepository.findById(userId).get()));
        return jsonObject;
    }

    public void delete(int reviewId, int warehouseId) throws Exception {
        reviewsRepository.deleteByReviewIdAndWarehouseId(reviewId, warehouseId);
    }
}
