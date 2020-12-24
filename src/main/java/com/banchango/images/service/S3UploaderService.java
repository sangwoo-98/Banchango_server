package com.banchango.images.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.banchango.auth.token.JwtTokenUtil;
import com.banchango.common.dto.BasicMessageResponseDto;
import com.banchango.common.exception.InternalServerErrorException;
import com.banchango.domain.warehouseimages.WarehouseImages;
import com.banchango.domain.warehouseimages.WarehouseImagesRepository;
import com.banchango.domain.warehouses.Warehouses;
import com.banchango.domain.warehouses.WarehousesRepository;
import com.banchango.images.dto.ImageInfoResponseDto;
import com.banchango.warehouses.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class S3UploaderService {

    private AmazonS3 s3Client;
    private final WarehouseImagesRepository warehouseImagesRepository;
    private final WarehousesRepository warehousesRepository;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.access_key_id}")
    private String accessKey;

    @Value("${aws.secret_access_key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    private String uploadFile(MultipartFile file) {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            byte[] bytes = IOUtils.toByteArray(file.getInputStream());
            objectMetadata.setContentLength(bytes.length);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            String fileName = file.getOriginalFilename();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, byteArrayInputStream, objectMetadata);
            s3Client.putObject(putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead));
            return s3Client.getUrl(bucket, fileName).toString();
        } catch(IOException exception) {
            throw new InternalServerErrorException();
        }
    }

    private void deleteFile(final String fileName) {
        final DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, fileName);
        try {
            s3Client.deleteObject(deleteObjectRequest);
        } catch(Exception exception) {
            throw new InternalServerErrorException();
        }
    }

    private boolean isUserAuthenticatedToModifyWarehouseInfo(Integer userId, Integer warehouseId) {
        List<Warehouses> warehouses = warehousesRepository.findByUserId(userId);
        for(Warehouses warehouse : warehouses) {
            if(warehouse.getId().equals(warehouseId)) {
                if(warehouse.getUserId().equals(userId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Transactional
    public ImageInfoResponseDto uploadExtraImage(MultipartFile file, String token, Integer warehouseId) {
        if(!isUserAuthenticatedToModifyWarehouseInfo(JwtTokenUtil.extractUserId(token), warehouseId)) {
            throw new WarehouseInvalidAccessException();
        }
        if(warehouseImagesRepository.findByWarehouseIdAndIsMain(warehouseId, 0).size() >= 5) {
            throw new WarehouseExtraImageLimitException();
        }
        Warehouses warehouse = warehousesRepository.findById(warehouseId).orElseThrow(WarehouseIdNotFoundException::new);
        String url = uploadFile(file);
        WarehouseImages image = WarehouseImages.builder().url(url).isMain(0).warehouse(warehouse).build();
        WarehouseImages savedImage = warehouseImagesRepository.save(image);
        return new ImageInfoResponseDto(savedImage);
    }

    @Transactional
    public ImageInfoResponseDto uploadMainImage(MultipartFile file, String token, Integer warehouseId) {
        if(!isUserAuthenticatedToModifyWarehouseInfo(JwtTokenUtil.extractUserId(token), warehouseId)) {
            throw new WarehouseInvalidAccessException();
        }
        if(warehouseImagesRepository.findByWarehouseIdAndIsMain(warehouseId, 1).size() >= 1) {
            throw new WarehouseMainImageAlreadyRegisteredException();
        }
        Warehouses warehouse = warehousesRepository.findById(warehouseId).orElseThrow(WarehouseIdNotFoundException::new);
        String url = uploadFile(file);
        WarehouseImages image = WarehouseImages.builder().url(url).isMain(1).warehouse(warehouse).build();
        WarehouseImages savedImage = warehouseImagesRepository.save(image);
        return new ImageInfoResponseDto(savedImage);
    }

    @Transactional
    public BasicMessageResponseDto deleteExtraImage(String fileName, String token, Integer warehouseId) {
        if(!isUserAuthenticatedToModifyWarehouseInfo(JwtTokenUtil.extractUserId(token), warehouseId)) {
            throw new WarehouseInvalidAccessException();
        }
        if(warehouseImagesRepository.findByUrlContaining(fileName).isPresent()) {
            warehouseImagesRepository.deleteByUrlContaining(fileName);
            deleteFile(fileName);
            return new BasicMessageResponseDto("삭제에 성공했습니다.");
        } else {
            throw new WarehouseExtraImageNotFoundException(fileName + "은(는) 저장되어 있지 않은 사진입니다.");
        }
    }

    @Transactional
    public BasicMessageResponseDto deleteMainImage(String token, Integer warehouseId) {
        if(!isUserAuthenticatedToModifyWarehouseInfo(JwtTokenUtil.extractUserId(token), warehouseId)) {
            throw new WarehouseInvalidAccessException();
        }
        if(warehouseImagesRepository.findByWarehouseIdAndIsMain(warehouseId, 1).size() >= 1) {
            WarehouseImages image = warehouseImagesRepository.findByWarehouseIdAndIsMain(warehouseId, 1).get(0);
            String[] splitTemp = image.getUrl().split("/");
            String fileName = splitTemp[splitTemp.length - 1];
            deleteFile(fileName);
            return new BasicMessageResponseDto("삭제에 성공했습니다.");
        } else {
            throw new WarehouseMainImageNotFoundException();
        }
    }
//
//    @Transactional
//    public JSONObject deleteMainImage(String token, Integer warehouseId) throws Exception {
//        checkTokenAndWarehouseId(token, warehouseId);
//        WarehouseMainImages image = warehouseMainImagesRepository.findByWarehouseId(warehouseId).orElseThrow(WarehouseMainImageNotFoundException::new);
//        JSONObject jsonObject = ObjectMaker.getJSONObject();
//        String[] splitTemp = image.getMainImageUrl().split("/");
//        String fileName = splitTemp[splitTemp.length - 1];
//        deleteFileOnS3(fileName);
//        warehouseMainImagesRepository.deleteByWarehouseId(warehouseId);
//        jsonObject.put("message", "창고의 메인 이미지가 정상적으로 삭제되었습니다.");
//        return jsonObject;
//    }
}
