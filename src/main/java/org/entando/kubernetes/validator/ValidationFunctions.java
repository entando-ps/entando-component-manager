package org.entando.kubernetes.validator;

import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;
import org.entando.kubernetes.exception.EntandoValidationException;
import org.entando.kubernetes.service.digitalexchange.BundleUtilities;

@UtilityClass
public class ValidationFunctions {

    public static final String HOST_MUST_START_AND_END_WITH_ALPHANUMERIC_REGEX = "^[a-zA-Z0-9].*[a-zA-Z0-9]$";
    public static final Pattern HOST_MUST_START_AND_END_WITH_ALPHANUMERIC_REGEX_PATTERN = Pattern.compile(
            HOST_MUST_START_AND_END_WITH_ALPHANUMERIC_REGEX);

    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final List<String> VALID_PROTOCOLS = List.of(HTTP_PROTOCOL, HTTPS_PROTOCOL);

    /**
     * if the url uses the git or ssh protocol, replace it with http
     * validate the received url using url regex. checks that the url is not empty. if this fails, throw
     * EntandoValidationException with nullError message checks that the url is valid. if this fails, throw
     * EntandoValidationException with invalidError message
     *
     * @param stringUrl    the string contianing the url to validate
     * @param nullError    the message to add to the EntandoValidationException if the url is empty
     * @param invalidError the message to add to the EntandoValidationException if the url is not compliant
     * @return the received url
     */
    public static String composeUrlForcingHttpProtocolOrThrow(String stringUrl, String nullError, String invalidError) {
        final String httpProtocolUrl = BundleUtilities.gitSshProtocolToHttp(stringUrl);
        composeUrlOrThrow(httpProtocolUrl, nullError, invalidError);
        return stringUrl;
    }

    /**
     * validate the received url using url regex. checks that the url is not empty. if this fails, throw
     * EntandoValidationException with nullError message checks that the url is valid. if this fails, throw
     * EntandoValidationException with invalidError message
     *
     * @param stringUrl    the string contianing the url to validate
     * @param nullError    the message to add to the EntandoValidationException if the url is empty
     * @param invalidError the message to add to the EntandoValidationException if the url is not compliant
     * @return the url as java.net.URL
     */
    public static URL composeUrlOrThrow(String stringUrl, String nullError, String invalidError) {

        if (ObjectUtils.isEmpty(stringUrl)) {
            throw new EntandoValidationException(nullError);
        }

        URL url;
        try {
            url = new URL(stringUrl);
            url.toURI();
        } catch (Exception e) {
            throw new EntandoValidationException(invalidError + ": " + stringUrl);
        }

        if (!VALID_PROTOCOLS.contains(url.getProtocol())) {
            throw new EntandoValidationException(
                    invalidError + ": " + url + " - Protocol supported are : " + String.join(",", VALID_PROTOCOLS));
        }

        if (! HOST_MUST_START_AND_END_WITH_ALPHANUMERIC_REGEX_PATTERN.matcher(url.getHost()).matches()) {
            throw new EntandoValidationException(
                    invalidError + ": " + url + " - Hostname must start and finish with an alphanumeric character");
        }

        return url;
    }
}
