package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberFacade {

    private final MemberService memberService;
    private final LogRepository logRepository;

    public void LogExceptionSeparation(String username) {
        memberService.joinV3(username);
        logExceptionCatch(username);
    }

    private void logExceptionCatch(String username) {
        Log logMessage = new Log(username);
        log.info("--- logRepository 호출 시작 ---");
        try {
            logRepository.save(logMessage);
        } catch (RuntimeException e) {
            log.info("로그 저장에 실패하였습니다. logMessage={}", logMessage.getMessage());
            log.info("정상 흐름 반환");
        }
        log.info("--- logRepository 호출 종료 ---");
    }
}
