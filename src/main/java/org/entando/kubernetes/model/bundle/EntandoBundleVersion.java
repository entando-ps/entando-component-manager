package org.entando.kubernetes.model.bundle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.zafarkhaja.semver.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude
@Accessors(chain = true)
public class EntandoBundleVersion {

    private String version;
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private Version semVersion;
    //private ZonedDateTime timestamp;

    public static EntandoBundleVersion fromEntity(String version) {
        return new EntandoBundleVersion()
                .setVersion(version);
                //.timestamp() TODO how to read from k8s custom model?
    }

    public EntandoBundleVersion setVersion(String version) {
        this.version = version;
        this.semVersion = Version.valueOf(version.replaceAll("^v", ""));
        return this;
    }

}
