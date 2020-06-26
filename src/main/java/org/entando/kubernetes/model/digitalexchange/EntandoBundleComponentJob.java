package org.entando.kubernetes.model.digitalexchange;

import java.util.UUID;
import javax.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.entando.kubernetes.model.bundle.installable.Installable;

@Data
@Entity
@NoArgsConstructor
@Table(name = "entando_bundle_component_jobs")
public class EntandoBundleComponentJob {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "parent_entando_bundle_job_id")
    private EntandoBundleJob job;

    @Column(name = "component_type")
    @Enumerated(EnumType.STRING)
    private ComponentType componentType;

    @Column(name = "name")
    private String name;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    // metadata?

    @PrePersist
    public void generateId() {
        this.id = UUID.randomUUID();
    }

    @Transient
    private Installable installable;

    public EntandoBundleComponentJob duplicate() {
        EntandoBundleComponentJob newComponent = new EntandoBundleComponentJob();
        newComponent.setName(getName());
        newComponent.setJob(getJob());
        newComponent.setStatus(getStatus());
        newComponent.setComponentType(getComponentType());
        newComponent.setChecksum(getChecksum());
        newComponent.setErrorMessage(getErrorMessage());
        return newComponent;
    }

}
