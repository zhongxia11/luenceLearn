package starter;

import javax.servlet.Filter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

//exclude={DataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class}
@SpringBootApplication()
@ComponentScan({"com.lucene"})
public class Application extends WebMvcConfigurationSupport {

	private static Class<Application> applicationClass = Application.class;
	
	//@Override
    //protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
     //   return application.sources(applicationClass);
   // }
	
	@Bean
	public Filter initializeHttpPutHandler() {
		return new HttpPutFormContentFilter();
	}
	
	@Bean
	public CharacterEncodingFilter initializeCharacterEncodingFilter() {
		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding("UTF-8");
		filter.setForceEncoding(true);
		return filter;
	}

	
	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
	    builder.headless(false).run(args);
		try {
			Class.forName("nearRealTime.Searcher");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(true)
				.setUseTrailingSlashMatch(true);
	}


	
}
