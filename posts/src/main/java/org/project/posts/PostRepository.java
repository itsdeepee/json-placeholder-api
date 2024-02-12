package org.project.posts;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface PostRepository extends ListCrudRepository<Post, Integer> {

    @Override
    Optional<Post> findById(Integer integer);

    Post findByTitle(String title);
}
