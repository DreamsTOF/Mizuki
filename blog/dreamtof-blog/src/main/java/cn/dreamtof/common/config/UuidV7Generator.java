package cn.dreamtof.common.config;

import com.github.f4b6a3.uuid.UuidCreator;
import com.mybatisflex.core.keygen.IKeyGenerator;

import java.util.UUID;

public class UuidV7Generator implements IKeyGenerator {
    @Override
    public Object generate(Object entity, String column) {
        UUID uuid = UuidCreator.getTimeOrderedEpoch();
        System.out.println("===== UuidV7Generator.generate() called, entity: " + entity.getClass().getSimpleName() + ", column: " + column + ", uuid: " + uuid);
        return uuid;
    }
}