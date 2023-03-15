package de.medizininformatikinitiative.flare.model;

import de.medizininformatikinitiative.flare.service.SerializerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PopulationTest {

    static final String PATIENT_ID = "patient-id-211701";

    @Test
    void toByteBuffer_empty() {
        var population = Population.of();

        var byteBuffer = population.toByteBuffer();

        assertEquals(1, byteBuffer.capacity());
        assertEquals(1, byteBuffer.remaining(), "the buffer is ready to be read");
        assertEquals(0, byteBuffer.get(), "version byte");
    }

    @Test
    void toByteBuffer_one_patientId() {
        var population = Population.of(PATIENT_ID);

        var byteBuffer = population.toByteBuffer();

        assertEquals(2 + PATIENT_ID.length(), byteBuffer.capacity());
        assertEquals(2 + PATIENT_ID.length(), byteBuffer.remaining(), "the buffer is ready to be read");
        assertEquals(0, byteBuffer.get(), "version byte");
        assertEquals(PATIENT_ID.length(), byteBuffer.get(), "patient id length");
        assertEquals('p', byteBuffer.get(), "first patient id byte");
        assertEquals('a', byteBuffer.get(), "second patient id byte");
        assertEquals('t', byteBuffer.get(), "third patient id byte");
    }

    @ParameterizedTest
    @MethodSource("provideCacheValues")
    void byteBuffer_roundTrip(Population population) throws SerializerException {
        var byteBuffer = population.toByteBuffer();

        assertEquals(population, Population.fromByteBuffer(byteBuffer));
    }

    private static Stream<Arguments> provideCacheValues() {
        return IntStream.range(0, 12)
                .map(n -> ((int) Math.pow(4, n)))
                .mapToObj(PopulationTest::populationOfSize)
                .map(Arguments::of);
    }

    private static Population populationOfSize(int n) {
        return Population.copyOf(IntStream.range(0, n).mapToObj("patient-id-%d"::formatted).collect(Collectors.toSet()));
    }
}
