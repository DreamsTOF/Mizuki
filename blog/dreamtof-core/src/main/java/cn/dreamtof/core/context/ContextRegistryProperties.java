package cn.dreamtof.core.context;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Setter
@Getter
public class ContextRegistryProperties {

    private boolean enabled = false;

}
