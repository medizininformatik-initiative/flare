package de.medizininformatikinitiative.flare.model;

import de.medizininformatikinitiative.flare.service.SerializerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.com.google.common.hash.Hashing;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PopulationTest {

    static final String PATIENT_ID = "patient-id-211701";
    static final String PATIENT_ID_1 = "patient-id-1-131300";
    static final String PATIENT_ID_2 = "patient-id-2-131309";

    @Test
    void of_onePatientId() {
        var population = Population.of(PATIENT_ID);

        assertThat(population).containsOnly(PATIENT_ID);
    }

    @Test
    void of_twoDifferentPatientIds() {
        var population = Population.of(PATIENT_ID_1, PATIENT_ID_2);

        assertThat(population).containsOnly(PATIENT_ID_1, PATIENT_ID_2);
    }

    @Test
    void of_twoSamePatientIds() {
        assertThatThrownBy(() -> Population.of(PATIENT_ID, PATIENT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("duplicate patient id: " + PATIENT_ID);
    }

    @Test
    void copyOf_onePatientId() {
        var population = Population.copyOf(List.of(PATIENT_ID));

        assertThat(population).containsOnly(PATIENT_ID);
    }

    @Test
    void copyOf_twoDifferentPatientIds() {
        var population = Population.copyOf(List.of(PATIENT_ID_1, PATIENT_ID_2));

        assertThat(population).containsOnly(PATIENT_ID_1, PATIENT_ID_2);
    }

    @Test
    void copyOf_twoSamePatientIds() {
        var population = Population.copyOf(List.of(PATIENT_ID, PATIENT_ID));

        assertThat(population).containsExactly(PATIENT_ID);
    }

    @ParameterizedTest
    @MethodSource("patientIdsArgumentProvider")
    void copyOf_more(List<String> patientIds) {
        var population = Population.copyOf(patientIds);

        assertThat(population).doesNotHaveDuplicates().hasSameElementsAs(patientIds);
    }

    @ParameterizedTest
    @MethodSource("populationArgumentsProvider")
    void intersection(Population population1, Population population2) {
        var expected = new HashSet<>(population1);
        expected.retainAll(population2);

        var result = population1.intersection(population2);

        assertThat(result).doesNotHaveDuplicates().hasSameElementsAs(expected);
    }

    @Test
    @DisplayName("the oldest create instant is transferred to the intersection result")
    void intersection_created() {
        var population1 = Population.of(PATIENT_ID_1).withCreated(Instant.EPOCH.plusSeconds(2));
        var population2 = Population.of(PATIENT_ID_2).withCreated(Instant.EPOCH.plusSeconds(1));

        var result = population1.intersection(population2);

        assertThat(result.created()).isEqualTo(Instant.EPOCH.plusSeconds(1));
    }

    @ParameterizedTest
    @MethodSource("populationArgumentsProvider")
    void union(Population population1, Population population2) {
        var expected = new HashSet<>(population1);
        expected.addAll(population2);

        var result = population1.union(population2);

        assertThat(result)
                .doesNotHaveDuplicates()
                .hasSameElementsAs(expected)
                .hasSizeBetween(Math.min(population1.size(), population2.size()), population1.size() + population2.size());
    }

    @Test
    @DisplayName("the oldest create instant is transferred to the union result")
    void union_created() {
        var population1 = Population.of(PATIENT_ID_1).withCreated(Instant.EPOCH.plusSeconds(2));
        var population2 = Population.of(PATIENT_ID_2).withCreated(Instant.EPOCH.plusSeconds(1));

        var result = population1.union(population2);

        assertThat(result.created()).isEqualTo(Instant.EPOCH.plusSeconds(1));
    }

    @ParameterizedTest
    @MethodSource("populationArgumentsProvider")
    void difference(Population population1, Population population2) {
        var expected = new HashSet<>(population1);
        expected.removeAll(population2);

        var result = population1.difference(population2);

        assertThat(result).doesNotHaveDuplicates().hasSameElementsAs(expected);
    }

    @Test
    @DisplayName("the oldest create instant is transferred to the difference result")
    void difference_created() {
        var population1 = Population.of(PATIENT_ID_1).withCreated(Instant.EPOCH.plusSeconds(2));
        var population2 = Population.of(PATIENT_ID_2).withCreated(Instant.EPOCH.plusSeconds(1));

        var result = population1.difference(population2);

        assertThat(result.created()).isEqualTo(Instant.EPOCH.plusSeconds(1));
    }

    @Test
    @DisplayName("populations with different created instants are different")
    void equals_differentCreatedInstants() {
        var population = Population.of().withCreated(Instant.ofEpochSecond(0));

        assertThat(population).isNotEqualTo(Population.of().withCreated(Instant.ofEpochSecond(1)));
    }

    @Test
    void toByteBuffer_empty() {
        var population = Population.of();

        var byteBuffer = population.toByteBuffer();

        assertEquals(13, byteBuffer.capacity());
        assertEquals(13, byteBuffer.remaining(), "the buffer is ready to be read");
        assertEquals(0, byteBuffer.get(), "version byte");
        assertEquals(0, byteBuffer.getLong(), "created instance");
        assertEquals(0, byteBuffer.getInt(), "size");
    }

    @Test
    void toByteBuffer_one_patientId() {
        var population = Population.of(PATIENT_ID);

        var byteBuffer = population.toByteBuffer();

        assertEquals(14 + PATIENT_ID.length(), byteBuffer.capacity());
        assertEquals(14 + PATIENT_ID.length(), byteBuffer.remaining(), "the buffer is ready to be read");
        assertEquals(0, byteBuffer.get(), "version byte");
        assertEquals(0, byteBuffer.getLong(), "created instance");
        assertEquals(1, byteBuffer.getInt(), "size");
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
        return IntStream.range(0, 11)
                .map(n -> ((int) Math.pow(4, n)))
                .mapToObj(PopulationTest::populationOfSize)
                .map(Arguments::of);
    }

    private static Population populationOfSize(int n) {
        return Population.copyOf(patientIdsOfSize(n))
                .withCreated(Instant.ofEpochSecond(n));
    }

    /**
     * Returns a list of {@code n} patient ids were every two patient ids are identical.
     *
     * @param n the number of patient ids to generate
     * @return a list of {@code n} patient ids
     */
    private static List<String> patientIdsOfSize(int n) {
        return IntStream.range(0, n).mapToObj(i -> Hashing.sha256().newHasher().putInt(i / 2).hash().toString()).toList();
    }

    private static Stream<Arguments> populationArgumentsProvider() {
        Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (Population p1 : populations()) {
            for (Population p2 : populations()) {
                argumentBuilder.add(Arguments.of(p1, p2));
            }
        }
        return argumentBuilder.build();
    }

    private static Stream<List<String>> patientIdsArgumentProvider() {
        return IntStream.range(0, 100).mapToObj(PopulationTest::patientIdsOfSize);
    }

    private static List<Population> populations() {
        return IntStream.range(0, 100).mapToObj(PopulationTest::populationOfSize).toList();
    }
}
