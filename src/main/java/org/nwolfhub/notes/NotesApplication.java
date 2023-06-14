package org.nwolfhub.notes;

import org.nwolfhub.easycli.Defaults;
import org.nwolfhub.easycli.EasyCLI;
import org.nwolfhub.easycli.model.FlexableValue;
import org.nwolfhub.notes.model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.format.DateTimeFormatter;
import java.util.Date;

@SpringBootApplication
public class NotesApplication {
	public static EasyCLI cli = new EasyCLI();

	public static void main(String[] args) {
		cli.addTemplate(Defaults.defaultTemplate);
		SpringApplication.run(NotesApplication.class, args);
	}

}
