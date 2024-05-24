package kr.co.dominos.demo.service;

import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
public class SseService {
    //동시성이 요구되는 환경에서는 ConcurrentHashMap을 사용하여 스레드 안전성을 보장하는 것이 좋다고 함
    private final Map<String,SseEmitter> emitters = new ConcurrentHashMap<>();


    //연결시도 수신 및 메세지 전송
    public SseEmitter connect(String id){
        SseEmitter emitter = new SseEmitter();

        //사용자 연결 보관
        emitters.put(id,emitter);

        try{
            emitter.send(SseEmitter.event().id(id).data("[" + id + "] 님 환영합니다."));
        }catch(Exception e){
            e.printStackTrace();
        }

        return emitter;
    }

    //메세지 전송
    public void sendToClient(String id,String orderId, String status){
        SseEmitter emitter = emitters.getOrDefault(id,null);

        JSONObject jb = new JSONObject();
        jb.put("orderId",orderId);
        jb.put("status",status);

        if(emitter != null){
            try{
                emitter.send(SseEmitter.event().id(id).name("message").data(jb));
            }catch(Exception e){
                e.printStackTrace();
            }
        }else{
            log.info("[" + id + "] 는 연결 목록에 없는 사용자");

        }

    }

}
