package org.entando.kubernetes.model;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AbstractThumbnailImage {
    public static final String IMAGE_BASE64 ="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";
}
