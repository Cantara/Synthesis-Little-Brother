package {{groupId}};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class {{artifactId | pascal-case}}Application {

	public static void main(String[] args) {
		SpringApplication.run({{artifactId | pascal-case}}Application.class, args);
	}

}
