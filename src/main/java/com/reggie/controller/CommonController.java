package com.reggie.controller;

import com.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.javassist.compiler.ast.Variable;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

//@Controller
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.uploadPath}")   //D:\abc
    private String uploadPath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
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
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            //将临时文件转存到指定位置
            String filePath = uploadPath + "/" + fileName;
            log.info("filePath:{}",filePath);
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {

        try {
            //输入流。通过输入流读取文件
            FileInputStream inputStream = new FileInputStream(new File(uploadPath +"/"+ name));

            //输出流。通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            //设置相应的类型为图片
            response.setContentType("image/jpeg");

            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(bytes)) != -1) {  //当长度不等于-1时。一直读
                outputStream.write(bytes, 0, len); //读数组
                outputStream.flush();  //刷新
            }
            inputStream.close();
            outputStream.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
