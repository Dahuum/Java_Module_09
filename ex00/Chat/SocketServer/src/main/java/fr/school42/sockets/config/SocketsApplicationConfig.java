package fr.school42.sockets.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
@ComponentScan("fr.school42.sockets")
public class SocketsApplicationConfig 
{
	@Bean
	public DataSource dataSource() {
		Properties props = loadProperties();

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(props.getProperty("db.url"));
		config.setUsername(props.getProperty("db.username"));
		config.setPassword(props.getProperty("db.password"));
		config.setDriverClassName(props.getProperty("db.driver.name"));

		return new HikariDataSource(config);
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private Properties loadProperties() {
		Properties props = new Properties();
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
			if (input == null) throw new RuntimeException("Unable to find db.properties.");

			props.load(input);
		} catch (IOException e) { throw new RuntimeException("Error loading fb.properties.", e); }
		return props;
	}
}
