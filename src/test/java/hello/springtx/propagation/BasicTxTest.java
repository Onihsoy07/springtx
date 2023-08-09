package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager tm;

    @TestConfiguration
    static class BasicTxTestConfig {

        @Bean
        public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = tm.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        tm.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = tm.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        tm.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }

    @Test
    void doubleCommit() {
        log.info("트랜잭션1 시작");
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션1 커밋 시작");
        tm.commit(status1);
        log.info("트랜잭션1 커밋 완료");

        log.info("트랜잭션2 시작");
        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션2 커밋 시작");
        tm.commit(status2);
        log.info("트랜잭션2 커밋 완료");
    }

    @Test
    void commit_rollback() {
        log.info("트랜잭션1 시작");
        TransactionStatus status1 = tm.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션1 커밋 시작");
        tm.commit(status1);
        log.info("트랜잭션1 커밋 완료");

        log.info("트랜잭션2 시작");
        TransactionStatus status2 = tm.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션2 롤백 시작");
        tm.rollback(status2);
        log.info("트랜잭션2 롤백 완료");
    }

    @Test
    void innerCommit() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.insNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.insNewTransaction()={}", inner.isNewTransaction());

        log.info("내부 트랜잭션 커밋 시작");
        tm.commit(inner);
        log.info("내부 트랜잭션 커밋 완료");

        log.info("외부 트랜잭션 커밋 시작");
        tm.commit(outer);
        log.info("외부 트랜잭션 커밋 완료");
    }

    @Test
    void outerRollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.insNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.insNewTransaction()={}", inner.isNewTransaction());

        log.info("내부 트랜잭션 커밋 시작");
        tm.commit(inner);
        log.info("내부 트랜잭션 커밋 완료");

        log.info("외부 트랜잭션 롤백 시작");
        tm.rollback(outer);
        log.info("외부 트랜잭션 롤백 완료");
    }

    @Test
    void innerRollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.insNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.insNewTransaction()={}", inner.isNewTransaction());

        log.info("내부 트랜잭션 롤백 시작");
        tm.rollback(inner); //rollback-only 마킹
        log.info("내부 트랜잭션 롤백 완료");

        log.info("외부 트랜잭션 커밋 시작");
        assertThatThrownBy(() -> tm.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
        log.info("외부 트랜잭션 커밋 완료");
    }

    @Test
    void innerRollback_requires_new() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.insNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        DefaultTransactionAttribute defaultTransactionAttribute = new DefaultTransactionAttribute();
        //새로운 트랜잭션 생성 세팅
        defaultTransactionAttribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus inner = tm.getTransaction(defaultTransactionAttribute);
        log.info("inner.insNewTransaction()={}", inner.isNewTransaction());

        log.info("내부 트랜잭션 롤백 시작");
        tm.rollback(inner);
        log.info("내부 트랜잭션 롤백 완료");

        log.info("외부 트랜잭션 커밋 시작");
        tm.commit(outer);
        log.info("외부 트랜잭션 커밋 완료");
    }

}
