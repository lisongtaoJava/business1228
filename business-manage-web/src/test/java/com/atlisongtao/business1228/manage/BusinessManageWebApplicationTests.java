package com.atlisongtao.business1228.manage;





import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;


@SpringBootTest
class BusinessManageWebApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void textFileUpload() throws IOException, MyException {
		// 读取图片服务器的地址
		String file = this.getClass().getResource("/tracker.conf").getFile();
		ClientGlobal.init(file);
		// 客户端
		TrackerClient trackerClient=new TrackerClient();
		// 服务器
		TrackerServer trackerServer=trackerClient.getTrackerServer();
		StorageClient storageClient=new StorageClient(trackerServer,null);
		// 上传的图片
		String orginalFilename="c://testData//001.jpg";
		// upload /root/001.jpg
		String[] upload_file = storageClient.upload_file(orginalFilename, "jpg", null);
		for (int i = 0; i < upload_file.length; i++) {
			String s = upload_file[i];
			System.out.println("s = " + s);

			//s = group1
			// s = M00/00/00/wKjAgF4LTdOAQ8pEAAAl_GXv6Z4721.jpg
		}
	}
}





