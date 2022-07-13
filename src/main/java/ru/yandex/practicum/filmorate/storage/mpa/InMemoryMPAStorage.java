package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.*;

@Component
public class InMemoryMPAStorage implements MPAStorage {

    @Getter
    private final Map<Integer, MPA> ratings;
    private static int mpaId;

    public InMemoryMPAStorage() {
        ratings = new HashMap<>();
        ratings.put(generateId(), MPA.builder().id(mpaId).name("G").build());
        ratings.put(generateId(), MPA.builder().id(mpaId).name("PG").build());
        ratings.put(generateId(), MPA.builder().id(mpaId).name("PG-13").build());
        ratings.put(generateId(), MPA.builder().id(mpaId).name("R").build());
        ratings.put(generateId(), MPA.builder().id(mpaId).name("NC-17").build());
    }

    private int generateId() {
        return ++mpaId;
    }


    @Override
    public MPA create(MPA mpa) {
        mpa.setId(generateId());
        ratings.put(mpaId, mpa);
        return mpa;
    }

    @Override
    public Optional<MPA> update(MPA mpa) {
        if (ratings.containsKey(mpa.getId())) {
            ratings.put(mpa.getId(), mpa);
            return Optional.of(mpa);
        }
        return Optional.empty();
    }

    @Override
    public List<MPA> findAll() {
        return new ArrayList<>(ratings.values());
    }

    @Override
    public Optional<MPA> findById(long id) {
        return Optional.ofNullable(ratings.get(id));
    }

    @Override
    public boolean deleteById(long id) {
        return ratings.remove(id) != null;
    }
}
