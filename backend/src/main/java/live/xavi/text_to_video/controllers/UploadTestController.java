package live.xavi.text_to_video.controllers;

import live.xavi.text_to_video.services.MinioUploadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/devtest/upload")
public class UploadTestController {

    private final MinioUploadService uploadService;

    public UploadTestController(MinioUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @GetMapping
    public String testUpload() throws Exception {
        return uploadService.upload("/home/main/Projects/text-to-video/backend/temp_eb1d3ce1-e90a-4f42-a9af-760cee1a4ad9/final_video.mp4");
    }
}
