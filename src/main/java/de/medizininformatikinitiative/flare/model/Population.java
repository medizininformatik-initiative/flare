package de.medizininformatikinitiative.flare.model;

import de.medizininformatikinitiative.flare.service.SerializerException;

import java.nio.ByteBuffer;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * A Population is an immutable set of patient ids.
 * <p>
 * Patient ids can have a maximum length of 64 chars.
 * <p>
 * This implementation uses a list as storage to safe some memory.
 */
public final class Population extends AbstractSet<String> {

    private static final Population EMPTY = new Population(Set.of());

    private final Set<String> patientIds;

    private Population(Set<String> patientIds) {
        this.patientIds = Set.copyOf(patientIds);
    }

    public static Population of() {
        return EMPTY;
    }

    public static Population of(String patientId1) {
        checkPatientId(patientId1);
        return new Population(Set.of(patientId1));
    }

    public static Population of(String patientId1, String patientId2) {
        checkPatientId(patientId1);
        checkPatientId(patientId2);
        return new Population(Set.of(patientId1, patientId2));
    }

    public static Population copyOf(Set<String> patientIds) {
        for (String id : patientIds) {
            checkPatientId(id);
        }
        return new Population(patientIds);
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
        return new Population(ret);
    }

    public Population union(Population other) {
        var ret = new HashSet<>(patientIds);
        ret.addAll(other.patientIds);
        return new Population(ret);
    }

    public Population difference(Population other) {
        var ret = new HashSet<>(patientIds);
        other.patientIds.forEach(ret::remove);
        return new Population(ret);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Population p))
            return false;

        return patientIds.equals(p.patientIds);
    }

    @Override
    public int hashCode() {
        return patientIds.hashCode();
    }

    @Override
    public String toString() {
        return "Population[size=" + size() + "]";
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(serializedSize());
        byteBuffer.put((byte) 0); //version byte

        for (String id : patientIds) {
            byte[] bytes = id.getBytes(US_ASCII);
            byteBuffer.put((byte) bytes.length);
            byteBuffer.put(bytes);
        }

        return byteBuffer.flip();
    }

    private int serializedSize() {
        return patientIds.stream().mapToInt(id -> id.getBytes(US_ASCII).length + 1).sum() + 1;
    }

    public static Population fromByteBuffer(ByteBuffer byteBuffer) throws SerializerException {
        if (!byteBuffer.hasRemaining()) {
            throw new SerializerException("Can't deserialize an empty byte buffer.");
        }
        byte versionByte = byteBuffer.get();
        if (versionByte != 0) {
            throw new SerializerException("Can't deserialize because version %d isn't supported.".formatted(versionByte));
        }
        Set<String> patientIds = new HashSet<>();
        while (byteBuffer.remaining() > 0) {
            byte[] idBytes = new byte[byteBuffer.get()];
            byteBuffer.get(idBytes);
            patientIds.add(new String(idBytes, US_ASCII));
        }
        return new Population(patientIds);
    }

    private static void checkPatientId(String patientId1) {
        if (patientId1.length() > 64) {
            throw new IllegalArgumentException("Patient id `%s` is longer as 64 chars.".formatted(patientId1));
        }
    }
}
