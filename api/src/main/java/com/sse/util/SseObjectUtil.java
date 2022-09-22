package com.sse.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SseObjectUtil {

    /**
     * 해당 Job 형태 체크
     */
    public static boolean isNotEmpty(Object object) {
        if (Objects.isNull(object)) {
            return false;
        }

        boolean isNotEmpty = false;

        if (object instanceof List) {
            for (Object obj : (List) object) {
                if (Objects.nonNull(obj)) {
                    isNotEmpty = true;
                    break;
                }
            }
        } else if (object instanceof Map) {
            isNotEmpty = !((Map) object).isEmpty();
        } else if (object.getClass().isArray()) {
            isNotEmpty = ArrayUtils.isNotEmpty((Object[]) object);
        } else if (object instanceof String) {
            isNotEmpty = StringUtils.isNotEmpty((String) object);
        } else {
            isNotEmpty = true;
        }

        return isNotEmpty;
    }

}
