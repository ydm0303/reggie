package com.reggie.controller;

import com.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.uploadPath}")
    private String uploadPath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file是一个临时文件，需要存到指定位置，否则本次请求完成之后临时文件会删除
        log.info(file.toString());

        //原始文件名
        String originalFilename = file.getOriginalFilename(); //132.jpd
        //文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //为防止文件名相同，使用uuid随机生成
        String fileName = UUID.randomUUID().toString() + suffix;

        //创建一个目录
        File dir = new File(uploadPath);
        //判断目录是否存在
        if(!dir.exists()){
            dir.mkdirs();
        }

        try {
            //将临时文件转存到指定位置
            file.transferTo(new File(uploadPath + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);
    }
}
