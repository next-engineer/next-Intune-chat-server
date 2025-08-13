package com.next.intune.common.helper;

import com.next.intune.common.api.CustomException;
import com.next.intune.common.api.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class MySqlAdvisoryLock {
    private final JdbcTemplate jdbc;

    public <T> T withLock(String key, Duration timeout, Supplier<T> body) {
        Integer ok = jdbc.queryForObject("SELECT GET_LOCK(?, ?)", Integer.class, key, (int)timeout.getSeconds());
        if (ok == null || ok != 1) throw new CustomException(ResponseCode.TRY_AGAIN_LATER);
        try { return body.get(); }
        finally { jdbc.queryForObject("SELECT RELEASE_LOCK(?)", Integer.class, key); }
    }
}
