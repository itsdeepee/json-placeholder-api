package org.project.posts;







import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.TypeReference;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class PostDataLoader implements CommandLineRunner {

    Logger logger=LoggerFactory.getLogger(PostDataLoader.class);
    private final ObjectMapper objectMapper;
    private final PostRepository postRepository;

    @Override
    public void run(String... args) throws Exception {
        if(postRepository.count()==0){
            String POSTS_JSON="/data/posts.json";
            logger.info("Loading posts into database from JSON: {}",POSTS_JSON);
            try(InputStream inputStream= TypeReference.class.getResourceAsStream(POSTS_JSON)){
                Posts response=objectMapper.readValue(inputStream, Posts.class);
                postRepository.saveAll(response.posts());
            }catch (IOException e){
                throw new RuntimeException("Failed to read JSON data",e);
            }
        }
    }
}
