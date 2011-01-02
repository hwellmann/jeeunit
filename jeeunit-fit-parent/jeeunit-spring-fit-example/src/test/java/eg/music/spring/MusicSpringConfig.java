package eg.music.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MusicSpringConfig {
    
    @Bean
    public Browser browser() {
        return new Browser();
    }

    @Bean
    public Dialog dialog() {
        return new Dialog();
    }

    @Bean
    public Display display() {
        return new Display();
    }

    @Bean
    public MusicLibrary musicLibrary() {
        return new MusicLibrary();
    }

    @Bean
    public MusicPlayer musicPlayer() {
        return new MusicPlayer();
    }
    
    @Bean
    public Realtime realtime() {
        return new Realtime();
    }
    
    @Bean
    public Simulator simulator() {
        return new Simulator();
    }

}
