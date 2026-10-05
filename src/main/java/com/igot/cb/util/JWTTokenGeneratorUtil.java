package com.igot.cb.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;

import static com.igot.cb.constant.IntegrationConstant.*;

@Component
@Slf4j
public class JWTTokenGeneratorUtil {

    @Value("${jwt.secret.key}")
    private String jwtSecretKey;

    private final ObjectMapper objectMapper;

    public JWTTokenGeneratorUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generateRedisJwtTokenKey(Object requestBody, String url, String operationType) {
        String jwtToken = "";
        String reqJsonString = "";
        if (requestBody != null) {
            reqJsonString = convertJsonObjectToString(requestBody);
        }
        if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(operationType)) {
            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put(REQUEST_BODY_JSON_KEY, reqJsonString);
            payload.put(URL_JSON_KEY, url);
            payload.put(OPERATION_TYPE_JSON_KEY, operationType);

            jwtToken = JWT.create().withPayload(payload).sign(Algorithm.HMAC256(jwtSecretKey));
        }
        return jwtToken;
    }


    //convert RequestBody into String
    private String convertJsonObjectToString(Object requestBody) {
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            log.error("Error occurred while converting json object to json string", e);
        }
        return jsonString;
    }

    public String generateRedisJwtTokenKeyForFile(Flux<FilePart> files, Object requestBody, String url, String operationType) {
        String jwtToken = "";
        String reqJsonString = "";
        String reqFileString = "";
        if (requestBody != null) {
            reqJsonString = convertJsonObjectToString(requestBody);
        }
        if(files!=null){
            reqFileString=String.valueOf(files);
        }
        if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(operationType)) {
            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put(FLUX_FILE_KEY,reqFileString);
            payload.put(REQUEST_BODY_JSON_KEY, reqJsonString);
            payload.put(URL_JSON_KEY, url);
            payload.put(OPERATION_TYPE_JSON_KEY, operationType);

            jwtToken = JWT.create().withPayload(payload).sign(Algorithm.HMAC256(jwtSecretKey));
        }
        return jwtToken;
    }
}
