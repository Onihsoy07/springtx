package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;
    @Autowired
    MemberService memberService;

    /**
     * memberService       @Transaction : OFF
     * memberRepository    @Transaction : ON
     * logRepository       @Transaction : ON
     */
    @Test
    void joinV1() {
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //then
        assertTrue(memberRepository.findById(username).isPresent());
        assertTrue(logRepository.findById(username).isPresent());
    }

    /**
     * memberService       @Transaction : OFF
     * memberRepository    @Transaction : ON
     * logRepository       @Transaction : ON(Exception)
     *
     * member 저장, log 에러로 롤백(문제 됨)
     */
    @Test
    void outerTxOFF_fail() {
        //given
        String username = "로그예외_outerTxOff_fail";

        //when
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        //then
        assertTrue(memberRepository.findById(username).isPresent());
        assertTrue(logRepository.findById(username).isEmpty());
    }

    /**
     * memberService       @Transaction : ON
     * memberRepository    @Transaction : OFF
     * logRepository       @Transaction : OFF
     */
    @Test
    void singleTx() {
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //then
        assertTrue(memberRepository.findById(username).isPresent());
        assertTrue(logRepository.findById(username).isPresent());
    }

    /**
     * memberService       @Transaction : ON
     * memberRepository    @Transaction : ON
     * logRepository       @Transaction : ON
     */
    @Test
    void outerTxOn_success() {
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //then
        assertTrue(memberRepository.findById(username).isPresent());
        assertTrue(logRepository.findById(username).isPresent());
    }

    /**
     * memberService       @Transaction : ON
     * memberRepository    @Transaction : ON
     * logRepository       @Transaction : ON(Exception)
     *
     * member 저장, log 에러로 롤백(문제 됨)
     */
    @Test
    void outerTxOn_fail() {
        //given
        String username = "로그예외_outerTxOn_fail";

        //when
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        //then
        assertTrue(memberRepository.findById(username).isEmpty());
        assertTrue(logRepository.findById(username).isEmpty());
    }

    /**
     * memberService       @Transaction : ON
     * memberRepository    @Transaction : ON
     * logRepository       @Transaction : ON(Exception)
     *
     * memberService.joinV2 -> exception catch로 잡음
     * 논리 트랜잭션(LogRepository)에서 rollbackOnly=true를 하기 때문에 에러 잡아도 롤백 됨
     */
    @Test
    void recoverException_fail() {
        //given
        String username = "로그예외-recoverException_fail";

        //when
        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class); //내부 트랜잭션이 롤백 되었는데 외부 트랜잭션 커밋하면 UnexpectedRollbackException 발생

        //then
        assertTrue(memberRepository.findById(username).isEmpty());
        assertTrue(logRepository.findById(username).isEmpty());
    }

}