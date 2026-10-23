package ccrrs.model;

/**
 * Represents an evidence entity in the cybercrime network:
 * Suspect, Device IMEI, IP Address, Bank Account, or Phone/Email.
 * Serves as a vertex in the SuspectNetworkGraph.
 */
public class EvidenceEntity {
    public enum EntityType {
        SUSPECT,
        DEVICE_IMEI,
        IP_ADDRESS,
        BANK_ACCOUNT,
        PHONE_NUMBER
    }

    private final String id;
    private final String name;
    private final EntityType type;
    private final String metadata;

    public EvidenceEntity(String id, String name, EntityType type, String metadata) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EntityType getType() {
        return type;
    }

    public String getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s", type, id, name, metadata);
    }
}
