package ru.doccloud.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by ilya on 3/9/17.
 */
public class VersionHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionHelper.class);
    private static final String START_MINOR_VERSION_VALUE = "0.1";
    private static final double MINOR_VERSION_LAG = 0.1;
    public static String generateMinorDocVersion(final String oldMinorVersion){
        LOGGER.debug("old minor version {}", oldMinorVersion);
        if(StringUtils.isBlank(oldMinorVersion))
            return START_MINOR_VERSION_VALUE;

        final Double newMinorVersionNum = round(Double.parseDouble(oldMinorVersion) + MINOR_VERSION_LAG, 2);

        LOGGER.debug("new minor version {}", newMinorVersionNum);
        return String.valueOf(newMinorVersionNum);
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
