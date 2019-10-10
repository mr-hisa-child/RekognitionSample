package com.example.recognition;

import java.nio.ByteBuffer;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CreateCollectionRequest;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.InvalidParameterException;
import com.amazonaws.services.rekognition.model.ListCollectionsRequest;
import com.amazonaws.services.rekognition.model.ListCollectionsResult;
import com.amazonaws.services.rekognition.model.SearchFacesByImageRequest;
import com.amazonaws.services.rekognition.model.SearchFacesByImageResult;

@RestController
@RequestMapping(path = "/api/faces", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class FaceRestController {

    private static final String COLLECTION_ID = System.getenv("COLLECTION_ID");

    @PostMapping("regist")
    public IndexFacesResult regist(@RequestParam("upload_file") MultipartFile multipartFile,
            @RequestParam("id") String id) {

        try {

            AmazonRekognition rekognitionClient = getRekognitionClient();

            ListCollectionsResult listCollectionsResult = rekognitionClient
                    .listCollections(new ListCollectionsRequest());
            if (!listCollectionsResult.getCollectionIds().contains(COLLECTION_ID)) {
                rekognitionClient.createCollection(new CreateCollectionRequest().withCollectionId(COLLECTION_ID));
            }

            return rekognitionClient.indexFaces(
                    new IndexFacesRequest()
                            .withCollectionId(COLLECTION_ID)
                            .withExternalImageId(id)
                            .withImage(new Image().withBytes(ByteBuffer.wrap(multipartFile.getBytes()))));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("judge")
    public SearchFacesByImageResult diff(@RequestParam("upload_file") MultipartFile multipartFile) {

        try {

            AmazonRekognition rekognitionClient = getRekognitionClient();

            SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
                    .withCollectionId(COLLECTION_ID)
                    .withImage(new Image().withBytes(ByteBuffer.wrap(multipartFile.getBytes())))
                    .withFaceMatchThreshold(99.9F)
                    .withMaxFaces(1);

            return rekognitionClient
                    .searchFacesByImage(searchFacesByImageRequest);
        } catch (InvalidParameterException e) {
            return new SearchFacesByImageResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AmazonRekognition getRekognitionClient() {
        return AmazonRekognitionClientBuilder.standard()
                .withRegion("ap-northeast-1")
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();
    }

}