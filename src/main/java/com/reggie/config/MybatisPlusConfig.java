//package com.reggie.config;
//
//import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
//import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
//import org.springframework.boot.web.servlet.MultipartConfigFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.servlet.MultipartConfigElement;
//import java.io.File;
//
///**
// * MP配置类
// */
//@Configuration
//@Slf4j
//public class MybatisPlusConfig {
//
//    @Bean
//    public MybatisPlusInterceptor mybatisPlusInterceptor(){
//
//        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
//        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
//        return mybatisPlusInterceptor;
//    }
//
//
//    @Bean
//    public MultipartConfigElement multipartConfigElement(MultipartProperties multipartProperties){
//        MultipartConfigFactory configFactory = new MultipartConfigFactory();
//        String tempPath = System.getProperty("user.dir") + (StringUtils.isBlank(multipartProperties.getLocation()) ? "/temp" : multipartProperties.getLocation());
//        log.info("temp---------->"+tempPath);
//        File file = new File(tempPath);
//        if(!file.exists()){
//            file.mkdirs();
//        }
//        configFactory.setLocation(tempPath);
//        return configFactory.createMultipartConfig();
//    }
//}
