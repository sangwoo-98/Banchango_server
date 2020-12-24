package com.banchango.images.controller;

import com.banchango.common.dto.BasicMessageResponseDto;
import com.banchango.common.interceptor.ValidateRequired;
import com.banchango.images.dto.ImageInfoResponseDto;
import com.banchango.images.service.S3UploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class ImageController {

    private final S3UploaderService s3UploaderService;

    @ValidateRequired
    @PostMapping("/v3/images/upload/{warehouseId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ImageInfoResponseDto uploadImage(@RequestPart(name = "file") MultipartFile multipartFile,
                            @RequestAttribute(name = "accessToken") String accessToken,
                            @PathVariable Integer warehouseId) {
        return s3UploaderService.uploadExtraImage(multipartFile, accessToken, warehouseId);
    }

    @ValidateRequired
    @PostMapping("/v3/images/upload/main/{warehouseId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ImageInfoResponseDto uploadMainImage(@RequestPart(name = "file") MultipartFile multipartFile,
                                                @RequestAttribute(name = "accessToken") String accessToken,
                                                @PathVariable Integer warehouseId) {
        return s3UploaderService.uploadMainImage(multipartFile, accessToken, warehouseId);
    }

    @ValidateRequired
    @DeleteMapping("/v3/images/delete/{warehouseId}")
    @ResponseStatus(HttpStatus.OK)
    public BasicMessageResponseDto deleteMainImage(@RequestParam(name = "file") String fileName,
                                                   @RequestAttribute(name = "accessToken") String accessToken,
                                                   @PathVariable Integer warehouseId) {
        return s3UploaderService.deleteExtraImage(fileName, accessToken, warehouseId);
    }

    @ValidateRequired
    @DeleteMapping("/v3/images/delete/main/{warehouseId")
    public BasicMessageResponseDto deleteMainImage(@RequestAttribute(name = "accessToken") String accessToken,
                                                   @PathVariable Integer warehouseId) {
        return s3UploaderService.deleteMainImage(accessToken, warehouseId);
    }
//
//    // DONE
//    @DeleteMapping("/v2/images/delete/main/{warehouseId}")
//    public void deleteMainImage(@RequestHeader(name = "Authorization") String bearerToken,
//                                @PathVariable Integer warehouseId, HttpServletResponse response) {
//        try {
//            WriteToClient.send(response, s3UploaderService.deleteMainImage(bearerToken, warehouseId), HttpServletResponse.SC_OK);
//        } catch(AuthenticateException exception) {
//            WriteToClient.send(response, ObjectMaker.getJSONObjectWithException(exception), HttpServletResponse.SC_UNAUTHORIZED);
//        } catch(WarehouseInvalidAccessException exception) {
//            WriteToClient.send(response, ObjectMaker.getJSONObjectWithException(exception), HttpServletResponse.SC_FORBIDDEN);
//        } catch(WarehouseMainImageNotFoundException | WarehouseIdNotFoundException exception) {
//            WriteToClient.send(response, ObjectMaker.getJSONObjectWithException(exception), HttpServletResponse.SC_NOT_FOUND);
//        } catch(Exception exception) {
//            WriteToClient.send(response, ObjectMaker.getJSONObjectOfBadRequest(), HttpServletResponse.SC_BAD_REQUEST);
//        }
//    }
}
