package com.boot.swlugweb.v1.notice;

import com.boot.swlugweb.v1.blog.GoogleDriveService;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;
    
    private final GoogleDriveService googleDriveService;

    public NoticeController(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    @GetMapping
    public ResponseEntity<NoticePageResponseDto> getNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "10") int size
    ) {
        NoticePageResponseDto response = noticeService.getNoticesWithPagination(page, searchTerm, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detail")
    public ResponseEntity<NoticeDomain> getNoticeDetail(@RequestBody Map<String, String> request) {
        String id = request.get("id");

        NoticeDomain notice = noticeService.getNoticeDetail(id);
        return ResponseEntity.ok(notice);
    }
    
//    공지 저장
    @PostMapping("/save")
    public ResponseEntity<?> saveNotice(@RequestBody NoticeCreateDto noticeCreateDto,
                                        HttpSession session) {
        String userId = (String) session.getAttribute("USER");
//        String roleType = (String) session.getAttribute("ROLE");
//        System.out.println(roleType);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
//        // roleType이 null이거나 "admin"이 아닌 경우 401 Unauthorized 반환
//        if (roleType == null || !roleType.equalsIgnoreCase("admin")) {
//            return ResponseEntity.status(401).body("Unauthorized: Admin access required.");
//        }

        noticeService.createNotice(noticeCreateDto, userId);
        return ResponseEntity.ok().body("{\"redirect\": \"/api/notice\"}");
    }

//    //google 공지 저장
//    @PostMapping(value="/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> saveNotice(@RequestPart(name="noticeCreateDto") NoticeCreateDto noticeCreateDto,
//                                        @RequestPart(name = "imageFiles", required = false) List<MultipartFile> imageFiles,
//                                        HttpSession session) throws GeneralSecurityException, IOException  {
//        String userId = (String) session.getAttribute("USER");
////        String roleType = (String) session.getAttribute("ROLE");
////        System.out.println(roleType);
//        if (userId == null) {
//            return ResponseEntity.status(401).build();
//        }
////        // roleType이 null이거나 "admin"이 아닌 경우 401 Unauthorized 반환
////        if (roleType == null || !roleType.equalsIgnoreCase("admin")) {
////            return ResponseEntity.status(401).body("Unauthorized: Admin access required.");
////        }
//
//        try{
//            if(imageFiles != null && !imageFiles.isEmpty()){
//                noticeCreateDto.setImageFiles(imageFiles);
//            }
//            noticeService.createNotice(noticeCreateDto, userId);
////            return ResponseEntity.ok().body("{\"redirect\": \"/api/notice\"}");
//            return ResponseEntity.status(302)
//                    .header(HttpHeaders.LOCATION,"/api/notice")
//                    .build(); //blog 등록 리다이렉트
//        }catch (Exception e){
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//
//
//
//    }
    

    @PostMapping("/update")
    public ResponseEntity<String> updateNoticePost(
            @RequestBody NoticeUpdateRequestDto noticeUpdateRequestDto,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("USER");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        noticeService.updateNotice(noticeUpdateRequestDto, userId);
        return ResponseEntity.ok().body("{\"redirect\": \"/api/notice\"}");
    }

//    //google 수정
//    @PostMapping(value="/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<String> updateNoticePost(
//            @RequestPart(name="noticeUpdateRequestDto") NoticeUpdateRequestDto noticeUpdateRequestDto,
//            @RequestPart(name = "imageFiles", required = false) List<MultipartFile> imageFiles,
//            HttpSession session
//    )throws GeneralSecurityException, IOException  {
//        String userId = (String) session.getAttribute("USER");
//        if (userId == null) {
//            return ResponseEntity.status(401).build();
//        }
//        try {
//            if(imageFiles != null && !imageFiles.isEmpty()) {
//                noticeUpdateRequestDto.setImageFiles(imageFiles);
//            }
//            noticeService.updateNotice(noticeUpdateRequestDto, userId);
////            return ResponseEntity.ok().body("{\"redirect\": \"/api/notice\"}");
//            return ResponseEntity.status(302)
//                    .header(HttpHeaders.LOCATION,"/api/notice")
//                    .build(); //blog 수정 리다이렉트
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//
//    }
    

    @PostMapping("/delete")
    public ResponseEntity<String> deleteNoticePost(
            @RequestBody Map<String, String> request,
            HttpSession session
    ) throws GeneralSecurityException, IOException {
        String id = request.get("id");
        String userId = (String) session.getAttribute("USER");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        noticeService.deleteNotice(id, userId);
//        return ResponseEntity.ok().body("{\"redirect\": \"/api/notice\"}");
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION,"/api/notice")
                .build();
    }

    @PostMapping("/adjacent")
    public ResponseEntity<Map<String, NoticeSummaryDto>> getAdjacentNotices(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        Map<String, NoticeSummaryDto> adjacentNotices = noticeService.getAdjacentNotices(id);
        return ResponseEntity.ok(adjacentNotices);
    }

    //사진 업로드
    @PostMapping("/image/upload")
    @ResponseBody
    public String uploadImageToDrive(MultipartHttpServletRequest request, HttpServletRequest req) throws Exception {
        Map<String, Object> map = new HashMap<>();

        // 이미지 파일 받아오기
        MultipartFile uploadFile = request.getFile("upload");

        // GoogleDriveService를 통해 파일을 구글 드라이브에 업로드
        String fileUrl = googleDriveService.uploadFileToDrive(uploadFile);

        // 반환할 URL을 map에 넣어줌
        map.put("url", fileUrl);

        return new Gson().toJson(map);
    }
}
