package com.chainmarket.service.impl;

import com.chainmarket.service.IImageService;
import com.chainmarket.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;

@Service
public class ImageServiceImpl implements IImageService {
    
    private final String UPLOAD_URL = "https://imgbed.yiyunt.cn/api/upload";
    private final String CONFIG_URL = "https://imgbed.yiyunt.cn/api/upload_config";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // 先获取配置信息
            ResponseEntity<String> configResponse = restTemplate.getForEntity(CONFIG_URL, String.class);
            JsonNode config = objectMapper.readTree(configResponse.getBody());
            
            // 验证文件类型和大小
            if (config.has("code") && config.get("code").asInt() == 1) {
                // 验证文件类型
                String fileName = file.getOriginalFilename();
                String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                boolean isAllowedType = false;
                JsonNode allowTypes = config.get("allowtype");
                for (JsonNode type : allowTypes) {
                    if (type.asText().toLowerCase().equals(fileExt)) {
                        isAllowedType = true;
                        break;
                    }
                }
                if (!isAllowedType) {
                    throw new BusinessException("不支持的文件类型");
                }
                
                // 验证文件大小
                long maxSize = config.get("max_upload").asLong() * 1024; // 转换为字节
                if (file.getSize() > maxSize) {
                    throw new BusinessException("文件大小超过限制");
                }
            }
            
            // 准备上传请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            // 创建资源对象
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("fileupload", resource);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // 发送上传请求
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            System.out.println("准备发送请求到: " + UPLOAD_URL);
            ResponseEntity<String> response = restTemplate.postForEntity(
                UPLOAD_URL,
                requestEntity,
                String.class
            );
            
            String responseBody = response.getBody();
            System.out.println("图片上传响应: " + responseBody);
            
            if (responseBody == null) {
                throw new BusinessException("服务器返回空响应");
            }
            
            // 解析响应
            JsonNode root = objectMapper.readTree(responseBody);
            System.out.println("响应JSON: " + root.toString());
            
            if (root.has("success") && root.get("success").asBoolean() && root.has("url")) {
                String url = root.get("url").asText();
                System.out.println("获取到的图片URL: " + url);
                // 替换转义的斜杠
                url = url.replace("\\/", "/");
                if (url != null && !url.trim().isEmpty() && url.contains("imgbed.yiyunt.cn")) {
                    return url;
                }
            }
            
            if (root.has("success") && !root.get("success").asBoolean()) {
                String error = root.has("message") ? root.get("message").asText() : "未知错误";
                throw new BusinessException("图片上传失败: " + error);
            }
            
            throw new BusinessException("图片上传失败: 未获取到URL");
        } catch (BusinessException e) {
            System.out.println("业务异常: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("系统异常: " + e.getMessage());
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }
    }
} 