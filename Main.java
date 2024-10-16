package org.example;

import lombok.*;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
class Profession implements Serializable{
    private String name;
    private int baseArmor;

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Character> characters = new ArrayList<>();

    public void addCharacter(Character character) {
        this.characters.add(character);
        character.setProfession(this);
    }
}

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
class Character implements Serializable{
    private String name;
    private int level;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Profession profession;
}

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
class CharacterDto implements Comparable<CharacterDto>{
    private String name;
    private int level;
    private String profession;
    @Override
    public int compareTo(CharacterDto other) {
        return this.name.compareTo(other.name);
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        List<Profession> professions = initProf();

        professions.forEach(profession -> {
            System.out.println("Profession: " + profession.getName() + ", Base Armor: " + profession.getBaseArmor());
            profession.getCharacters().forEach(character -> {
                System.out.println("\tCharacter: " + character.getName() + ", Level: " + character.getLevel());
            });
        });

        Set<Character> allCharacters = professions.stream()
                .flatMap(profession -> profession.getCharacters().stream())
                .collect(Collectors.toSet());

        System.out.println("\nAll Characters in Set:");
        allCharacters.stream()
                .forEach(character -> System.out.println("Character: " + character.getName() + ", Level: " + character.getLevel() + ", Profession: " + character.getProfession().getName()));

        System.out.println("\nFiltered and Sorted Characters:");
        allCharacters.stream()
                .filter(character -> character.getLevel() > 10)
                .sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
                .forEach(character -> System.out.println("Character: " + character.getName() + ", Level: " + character.getLevel() + ", Profession: " + character.getProfession().getName()));

        List<CharacterDto> characterDtos = allCharacters.stream()
                .map(character -> CharacterDto.builder()
                        .name(character.getName())
                        .level(character.getLevel())
                        .profession(character.getProfession().getName())
                        .build())
                .sorted()
                .collect(Collectors.toList());

        System.out.println("\nCharacter DTO (sorted by name):");
        characterDtos.stream()
                .forEach(dto -> System.out.println("CharacterDto: Name = " + dto.getName() + ", Level = " + dto.getLevel() + ", Profession = " + dto.getProfession()));

        serializeProf(professions, "prof.bin");

        List<Profession> deserializedProf = deserializeProf("prof.bin");

        printProf(deserializedProf);

        System.out.println("\nThreads:");
        int poolSize = 2;
        ForkJoinPool ThreadPool = new ForkJoinPool(poolSize);

        ThreadPool.submit(() -> {
            professions.parallelStream().forEach(profession -> {
                System.out.println("Processing Profession: " + profession.getName() + ", Base Armor: " + profession.getBaseArmor());
                profession.getCharacters().forEach(character -> {
                    System.out.println("\tProcessing Character: " + character.getName() + ", Level: " + character.getLevel());

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
        });

        ThreadPool.shutdown();
        ThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }


    private static List<Profession> initProf() {
        Profession PA = Profession.builder().name("PA").baseArmor(50).build();
        Profession PB = Profession.builder().name("PB").baseArmor(30).build();

        Character character1 = Character.builder().name("A").level(10).build();
        Character character2 = Character.builder().name("B").level(15).build();
        Character character3 = Character.builder().name("C").level(12).build();

        PA.addCharacter(character1);
        PB.addCharacter(character2);
        PA.addCharacter(character3);

        List<Profession> professions = new ArrayList<>();
        professions.add(PA);
        professions.add(PB);

        return professions;
    }
    private static void serializeProf(List<Profession> professions, String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(professions);
            System.out.println("\nSerialized to " + filename);
        } catch (IOException e) {
            System.out.println("Error");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Profession> deserializeProf(String filename) {
        List<Profession> professions = null;
        try (FileInputStream fis = new FileInputStream(filename);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            professions = (List<Profession>) ois.readObject();
            System.out.println("Deserialized from " + filename);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error");
        }
        return professions;
    }

    private static void printProf(List<Profession> professions) {
        for (Profession profession : professions) {
            System.out.println("Profession: " + profession.getName() + ", Base Armor: " + profession.getBaseArmor());
            for (Character character : profession.getCharacters()) {
                System.out.println("\tCharacter: " + character.getName() + ", Level: " + character.getLevel());
            }
        }
    }
}
