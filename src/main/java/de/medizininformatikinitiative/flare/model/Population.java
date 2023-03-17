package de.medizininformatikinitiative.flare.model;

import de.medizininformatikinitiative.flare.service.SerializerException;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Objects.requireNonNull;

/**
 * A Population is an immutable set of patient ids.
 * <p>
 * Patient ids can have a maximum length of 64 chars.
 * <p>
 * This implementation uses a list as storage to safe some memory.
 */
public final class Population extends AbstractSet<String> {

    private static final Population EMPTY = new Population(Set.of(), Instant.EPOCH);

    private final Set<String> patientIds;
    private final Instant created;

    private Population(Set<String> patientIds, Instant created) {
        this.patientIds = Set.copyOf(patientIds);
        this.created = requireNonNull(created);
    }

    public static Population of() {
        return EMPTY;
    }

    public static Population of(String patientId1) {
        checkPatientId(patientId1);
        return new Population(Set.of(patientId1), Instant.EPOCH);
    }

    public static Population of(String patientId1, String patientId2) {
        checkPatientId(patientId1);
        checkPatientId(patientId2);
        return new Population(Set.of(patientId1, patientId2), Instant.EPOCH);
    }

    public static Population copyOf(Set<String> patientIds) {
        for (String id : patientIds) {
            checkPatientId(id);
        }
        return new Population(patientIds, Instant.EPOCH);
    }

    /**
     * Returns the instant at which this population was created.
     * <p>
     * Usually the created instant is used for cache expiry, but can be also used to determine how old a population is.
     *
     * @return the instant at which this population was created
     */
    public Instant created() {
        return created;
    }

    /**
     * Returns a new population with {@code created} set to the given value.
     *
     * @param created the instant to use as created instant for the new population
     * @return a new population with {@code created} set to the given value
     */
    public Population withCreated(Instant created) {
        return new Population(patientIds, created);
    }

    public boolean isEmpty() {
        return patientIds.isEmpty();
    }

    @Override
    public Iterator<String> iterator() {
        return patientIds.iterator();
    }

    @Override
    public int size() {
        return patientIds.size();
    }

    public int memSize() {
        return patientIds.stream().mapToInt(s -> 40 + s.length()).sum();
    }

    public Population intersection(Population other) {
        var ret = new HashSet<>(patientIds);
        ret.retainAll(other.patientIds);
        return new Population(ret, created.isBefore(other.created) ? created : other.created);
    }

    public Population union(Population other) {
        var ret = new HashSet<>(patientIds);
        ret.addAll(other.patientIds);
        return new Population(ret, created.isBefore(other.created) ? created : other.created);
    }

    public Population difference(Population other) {
        var ret = new HashSet<>(patientIds);
        other.patientIds.forEach(ret::remove);
        return new Population(ret, created.isBefore(other.created) ? created : other.created);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Population p))
            return false;

        return created.equals(p.created) && patientIds.equals(p.patientIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(created, patientIds);
    }

    @Override
    public String toString() {
        return "Population[size=" + size() + "]";
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(serializedSize());
        byteBuffer.put((byte) 0); //version byte

        byteBuffer.putLong(created.getEpochSecond());

        for (String id : patientIds) {
            byte[] bytes = id.getBytes(US_ASCII);
            byteBuffer.put((byte) bytes.length);
            byteBuffer.put(bytes);
        }

        return byteBuffer.flip();
    }

    private int serializedSize() {
        return patientIds.stream().mapToInt(id -> id.getBytes(US_ASCII).length + 1).sum() + 9;
    }

    public static Population fromByteBuffer(ByteBuffer byteBuffer) throws SerializerException {
        if (!byteBuffer.hasRemaining()) {
            throw new SerializerException("Can't deserialize an empty byte buffer.");
        }

        byte versionByte = byteBuffer.get();
        if (versionByte != 0) {
            throw new SerializerException("Can't deserialize because version %d isn't supported.".formatted(versionByte));
        }

        var created = Instant.ofEpochSecond(byteBuffer.getLong());

        Set<String> patientIds = new HashSet<>();
        while (byteBuffer.remaining() > 0) {
            byte[] idBytes = new byte[byteBuffer.get()];
            byteBuffer.get(idBytes);
            patientIds.add(new String(idBytes, US_ASCII));
        }
        return new Population(patientIds, created);
    }

    private static void checkPatientId(String patientId1) {
        if (patientId1.length() > 64) {
            throw new IllegalArgumentException("Patient id `%s` is longer as 64 chars.".formatted(patientId1));
        }
    }
}
