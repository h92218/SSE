package kr.co.dominos.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.dominos.demo.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/SSE")
public class SseController {

    private final SseService sseService;

    @GetMapping(value = "/subscribe/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(@PathVariable String id) {
        //생성자를 통해 만료시간 설정가능. 스프링 부트 내장톰캣 30초
        //만료시간이 되면 브라우저에서 자동으로 서버에 재연결 요청
        SseEmitter emitter = sseService.connect(id);

        //클라이언트와의 SSE연결을 설정하고 데이터를 전송하기 위한 스트림 생성
        return ResponseEntity.ok(emitter);
    }

    //메세지 보내기 테스트용
    @PostMapping(value = "/sendTest/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void sendTest(@PathVariable String id, HttpServletRequest request) {
        sseService.sendToClient(id,request.getParameter("orderId"),request.getParameter("status"));
    }


}
