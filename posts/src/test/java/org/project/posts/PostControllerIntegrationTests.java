package org.project.posts;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class PostControllerIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:16.0");



    @Autowired
   private TestRestTemplate restTemplate;

    @Test
    void shouldFindAllPosts(){
        // /api/posts
        Post[] posts=restTemplate.getForObject("/api/posts",Post[].class);
        assertThat(posts.length).isGreaterThan(100);
    }

    @Test
    void shouldFindPostWhenValidPostID(){
        ResponseEntity<Post> response=restTemplate.exchange("/api/posts/1", HttpMethod.GET,null,Post.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

    }

    @Test
    void shouldThrowNotFoundWhenInvalidPostID(){
        ResponseEntity<Post> response=restTemplate.exchange("/api/posts/999",HttpMethod.GET,null,Post.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }



    @Test
    @Rollback
    void shouldCreateNewPostWhenPostIsValid(){
        Post post=new Post(101,1,"Post title","post body",null);
        ResponseEntity<Post> response=restTemplate.exchange("/api/posts",HttpMethod.POST,new HttpEntity<Post>(post),Post.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).id()).isEqualTo(101);
        assertThat(response.getBody().userId()).isEqualTo(1);
        assertThat(response.getBody().title()).isEqualTo("Post title");
        assertThat(response.getBody().body()).isEqualTo("post body");
    }

    @Test
    void shouldNotCreateNewPostWhenValidationFails(){
        Post post=new Post(101,1,"","",null);
        ResponseEntity<Post> response=restTemplate.exchange("/api/posts",HttpMethod.POST,new HttpEntity<Post>(post),Post.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Rollback
    void shouldUpdatePostWhenPostIdValid(){
        ResponseEntity<Post> response=restTemplate.exchange("/api/posts/99",HttpMethod.GET,null,Post.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Post existing=response.getBody();
        assertThat(existing).isNotNull();
        Post updated=new Post(existing.id(),existing.userId(),"Update post","I'm updated",existing.version());

        assertThat(updated.id()).isEqualTo(99);
        assertThat(updated.userId()).isEqualTo(10);
        assertThat(updated.title()).isEqualTo("Update post");
        assertThat(updated.body()).isEqualTo("I'm updated");
    }

    @Test
    @Rollback
    void shouldDeleteWithValidID(){
        ResponseEntity<Void> response =restTemplate.exchange("/api/posts/88",HttpMethod.DELETE,null,Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }









}
