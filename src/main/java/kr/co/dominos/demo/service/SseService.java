package kr.co.dominos.demo.service;

import kr.co.dominos.demo.dao.RedisDao;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@Log4j2
@Service
@RequiredArgsConstructor
public class SseService {
    //동시성이 요구되는 환경에서는 ConcurrentHashMap을 사용하여 스레드 안전성을 보장하는 것이 좋다고 함
    //private final Map<String,SseEmitter> emitters = new ConcurrentHashMap<>();
    //private final Map<String, Map<String,SseEmitter> > emitterList1 = new ConcurrentHashMap<>();
    private final Map<String, Set<SseEmitter>> emitterList2 =  new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final RedisDao redisDao;

    //연결시도 수신 및 메세지 전송
    public SseEmitter connect(String id){
        SseEmitter emitter = new SseEmitter(600000L); //10분
        //SseEmitter emitter = new SseEmitter(5000L); //5초

        Set<SseEmitter> userEmitters = emitterList2.getOrDefault(id,new HashSet<>());
        try{

            userEmitters.add(emitter);
            emitterList2.put(id,userEmitters);

            //emitters 얼마나 있나 확인
            log.info(emitterList2.get(id));

            emitter.send(SseEmitter.event().id(id).data("[" + id + "] 님 환영합니다."));

        }catch(Exception e){
            log.info("응답 실패 :: " + id);
            e.printStackTrace();
        }


        emitter.onError((e)->{log.info("connect :: onError"); userEmitters.remove(emitter);});
        emitter.onTimeout(() -> {log.info("connect :: onTimeout"); userEmitters.remove(emitter); });

        //연결 종료 시 수행할 작업
        //emitter.onCompletion(() -> { log.info("connect :: onCompletion"); userEmitters.remove(emitter); });

        return emitter;
    }

    //메세지 전송
    public void sendToClient(String id,String orderId, String status){
        Set<SseEmitter> userEmitters = emitterList2.getOrDefault(id,null );

        JSONObject jb = new JSONObject();
        jb.put("orderId",orderId);
        jb.put("status",status);

        if(userEmitters != null && userEmitters.size() > 0){
            try{
                userEmitters.stream().forEach(emitter -> {
                    try {
                        emitter.send(SseEmitter.event().name("order").id(id).name("message").data(jb));
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.info("전송 실패 :: " + jb);
                    }
                });

            }catch(Exception e){
                log.info("전송 실패 :: " + jb);
                e.printStackTrace();
            }
        }else{
            log.info("[" + id + "] 는 연결 목록에 없는 사용자");
        }

    }

}
