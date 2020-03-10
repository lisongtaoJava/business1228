package com.atlisongtao.business1228.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// @Controller+@ResponseBody
@RestController
public class FileUploadController {

    @Value("${fileServer.url}") // 读取application.properties 的数据，fileServer.url ,使用@Value 注解，当前类则必须被spring容器扫描，否则得不到数据！
    private String fileUrl; //fileUrl =http://192.168.67.210

    /*
        回忆：springMVC fileUpload
            表单enType=multfile-from-data
            控制器
                @RequestParam("file") MultipartFile file
                该文件名称必须<input type="file" name="files">
            MultipartFile
            springmvc.xml 核心配置
                <bean  id="名称"：不能随意" class="org.springframework.web.multipart.MultipartFile"></bean>
     */
    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile file)throws IOException, MyException {
        String imgUrl = fileUrl;
        if (file!=null){
            // 读取图片服务器的地址
            String configFile  = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            // 客户端
            TrackerClient trackerClient=new TrackerClient();
            // 服务器
            TrackerServer trackerServer=trackerClient.getTrackerServer();
            StorageClient storageClient=new StorageClient(trackerServer,null);
            // 上传的图片
            // String orginalFilename="e://img//zly.jpg";
            String originalFilename = file.getOriginalFilename();
            // 如何取得文件上传的后缀名 zly.jpg
            String extName  = StringUtils.substringAfterLast(originalFilename, ".");
            // upload /root/001.jpg
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);

            for (int i = 0; i < upload_file.length; i++) {
//                String s = upload_file[i];
//                System.out.println("s = " + s);
                // s = group1
                // s = M00/00/00/wKhD0lv6zpCAYyFAAACGx2c4tJ4731.jpg
                String path = upload_file[i];
                imgUrl+="/"+path;
            }
        }
        // http://192.168.67.210/group1/M00/00/00/wKhD0lv6wF2AYRJZAAAl_GXv6Z4944.jpg
        // 添加谁，回显谁？
//  return "https://m.360buyimg.com/babel/jfs/t5137/20/1794970752/352145/d56e4e94/591417dcN4fe5ef33.jpg";
        return imgUrl;

    }

}
