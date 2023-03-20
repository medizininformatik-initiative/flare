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
 */
public final class Population extends AbstractSet<String> {

    private static final Population EMPTY = new Population(new HashSet<>(0), Instant.EPOCH);

    private final Set<String> patientIds;
    private final Instant created;

    private Population(Set<String> patientIds, Instant created) {
        this.patientIds = patientIds;
        this.created = created;
    }

    public static Population of() {
        return EMPTY;
    }

    public static Population of(String patientId1) {
        checkPatientId(patientId1);
        return new Population(Set.of(patientId1.intern()), Instant.EPOCH);
    }

    public static Population of(String patientId1, String patientId2) {
        checkPatientId(patientId1);
        checkPatientId(patientId2);
        return new Population(Set.of(patientId1.intern(), patientId2.intern()), Instant.EPOCH);
    }

    public static Population copyOf(Collection<String> patientIds) {
        for (String id : patientIds) {
            checkPatientId(id);
        }
        return new Population(Set.copyOf(patientIds.stream().map(String::intern).toList()), Instant.EPOCH);
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
        return new Population(patientIds, requireNonNull(created));
    }

    @Override
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

    /**
     * Returns the size of this population in memory.
     * <p>
     * The calculation assumes that an {@link Set#of immutable set} is used to tore the patient ids internally. In
     * addition it assumes that alle patient ids are {@link String#intern() interned}. Such sets use an array of double
     * the size of the set to store entries. Each entry is a reference to a string. References have a size of 4 bytes.
     * <p>
     * So the memory size is 24 bytes for the population class, 24 bytes for the instant, 24 bytes for the immutable set
     * and 8 times the size of the immutable set for the object array holding the string references. The strings itself
     * do not count, because they are interned.
     *
     * @return the size of this population in memory
     */
    public int memSize() {
        return 72 + patientIds.size() * 8;
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
        ret.removeAll(other.patientIds);
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

        var patientIds = new ArrayList<String>();
        while (byteBuffer.remaining() > 0) {
            byte[] idBytes = new byte[byteBuffer.get()];
            byteBuffer.get(idBytes);
            patientIds.add(new String(idBytes, US_ASCII).intern());
        }
        return new Population(Set.copyOf(patientIds), created);
    }

    private static void checkPatientId(String patientId1) {
        if (patientId1.length() > 64) {
            throw new IllegalArgumentException("Patient id `%s` is longer as 64 chars.".formatted(patientId1));
        }
    }
}
