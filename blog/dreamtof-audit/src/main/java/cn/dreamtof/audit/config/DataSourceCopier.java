package cn.dreamtof.audit.config;

import cn.dreamtof.core.context.ThreadContextCopier;
import com.mybatisflex.core.datasource.DataSourceKey;
import org.springframework.stereotype.Component;

@Component
public class DataSourceCopier implements ThreadContextCopier {

    @Override
    public Object capture() {
        return DataSourceKey.get();
    }

    @Override
    public void restore(Object contextSnapshot) {
        if (contextSnapshot != null) {
            DataSourceKey.use((String) contextSnapshot);
        }
    }

    @Override
    public void clear() {
        String currentKey = DataSourceKey.get();
        if (currentKey != null) {
            DataSourceKey.clear();
        }
    }
}
