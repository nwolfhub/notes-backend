package org.nwolfhub.notes;

import org.nwolfhub.easycli.Defaults;
import org.nwolfhub.easycli.EasyCLI;
import org.nwolfhub.notes.api.legacy.NotesController;
import org.nwolfhub.notes.api.legacy.UserController;
import org.nwolfhub.notes.database.legacy.TokenController;
import org.nwolfhub.notes.database.legacy.UserDao;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class NotesApplication {
	public static EasyCLI cli = new EasyCLI();

	public static void main(String[] args) {
		cli.addTemplate(Defaults.defaultTemplate);
		SpringApplication.run(NotesApplication.class, args);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Configurator.class);
		NotesController.init((org.nwolfhub.utils.Configurator) context.getBean("privilegesConfigurator"), context.getBean(UserDao.class));
		TokenController.init();
		UserController.init(context.getBean(UserDao.class));
	}

}
